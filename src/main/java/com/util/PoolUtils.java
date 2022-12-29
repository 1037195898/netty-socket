package com.util;

import com.interfaces.IPool;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.*;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * 对象池
 */
public class PoolUtils {
    private static final ConcurrentMap<Class<?>, GenericObjectPool<IPool>> poolMap = new ConcurrentHashMap<>();
    /** 对象池中最大存在数量 默认-1是int最大值 */
    public static int DEFAULT_MAX_TOTAL = GenericKeyedObjectPoolConfig.DEFAULT_MAX_TOTAL;
    public static int DEFAULT_MAX_IDLE = 100;
    public static int DEFAULT_MIN_IDLE = 1;
    /** 开启检查空闲和遗弃 */
    public static boolean OPEN_INSPECT_IDEA_REMOVE = true;
    /** 启动默认空闲和遗弃清理线程 默认5分半 要比超时场 */
    public static Duration DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS = Duration.ofSeconds(330);
    /** 设置为归还超时要被遗弃的秒数 默认5分钟 */
    public static Duration REMOVE_ABANDONED_TIMEOUT = Duration.ofSeconds(300);
    public static AbandonedConfig abandonedConfig;

    /**
     * 获取一个对象实例
     * @param cls
     * @return
     * @param <T>
     * @throws Exception 会超时报错 超时时间是  PoolUtils.REMOVE_ABANDONED_TIMEOUT
     */
    public static <T extends IPool> T get(Class<T> cls) throws Exception {
        return getObject(cls, REMOVE_ABANDONED_TIMEOUT);
    }

    /** 获取一个对象实例 */
    public static <T extends IPool> T getObject(Class<T> cls) throws Exception {
        return (T) getPool(cls).borrowObject();
    }

    /** 获取一个对象实例 */
    public static <T extends IPool> T getObject(Class<T> cls, final Duration borrowMaxWaitDuration) throws Exception {
        return (T) getPool(cls).borrowObject(borrowMaxWaitDuration);
    }

    /**
     * 获取一个对象实例
     * @param cls
     * @param borrowMaxWaitMillis 等待对象可用的时间（毫秒）
     * @return
     * @param <T>
     * @throws Exception
     */
    public static <T extends IPool> T getObject(Class<T> cls, long borrowMaxWaitMillis) throws Exception {
        return (T) getPool(cls).borrowObject(borrowMaxWaitMillis);
    }

    /** 归还对象 */
    public static  <T> void returnObject(IPool pool) {
        getPool(pool.getClass()).returnObject(pool);
    }

    /** 在池的生存期内为此池创建的对象总数。 */
    public static long getCreatedCount(Class cls) throws Exception {
        return getPool(cls).getCreatedCount();
    }

    /** 在池的生存期内返回到此池的对象总数。这排除了多次返回同一对象的尝试。 */
    public static long getReturnedCount(Class cls) throws Exception {
        return getPool(cls).getReturnedCount();
    }

    /** 此池在其生存期内销毁的对象总数。 */
    public static long getDestroyedCount(Class cls) throws Exception {
        return getPool(cls).getDestroyedCount();
    }

    /** 在池的生存期内，由于在borrowObject（）期间验证失败而被池销毁的对象总数。。 */
    public static long getDestroyedByBorrowValidationCount(Class cls) throws Exception {
        return getPool(cls).getDestroyedByBorrowValidationCount();
    }

    /** 在池的生存期内成功从此池借用的对象总数。 */
    public static long getBorrowedCount(Class cls) throws Exception {
        return getPool(cls).getBorrowedCount();
    }

    /** 获取此池中当前空闲的实例数 */
    public static long getNumIdle(Class cls) throws Exception {
        return getPool(cls).getNumIdle();
    }

    /** 获取当前从此池借用的实例数。 */
    public static long getNumActive(Class cls) throws Exception {
        return getPool(cls).getNumActive();
    }

    /** 获取指定对象的线程池 */
    public static GenericObjectPool<IPool> getPool(Class cls) {
        GenericObjectPool<IPool> pool = poolMap.get(cls);
        if (pool == null) {
            pool = new GenericObjectPool<>(new PoolObject<>(cls), createConfig());
            if (abandonedConfig == null) {
                abandonedConfig = new AbandonedConfig();
                // 开启池中已经饱和执行遗弃
                abandonedConfig.setRemoveAbandonedOnBorrow(true);
                // 活跃的对象也可以执行遗弃
                abandonedConfig.setRemoveAbandonedOnMaintenance(true);
                // 300秒未被回收的  遗弃掉
                abandonedConfig.setRemoveAbandonedTimeout(REMOVE_ABANDONED_TIMEOUT);
            }
            if (OPEN_INSPECT_IDEA_REMOVE) {
                TimerUtils.newTimeout(inspectTask, DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS.toMillis(), TimeUnit.MILLISECONDS);
            }
            pool.setAbandonedConfig(abandonedConfig);
            poolMap.put(cls, pool);
        }
        return pool;
    }

    static TimerTask inspectTask = new TimerTask() {
        @Override
        public void run(Timeout timeout) throws Exception {
            TimerUtils.newTimeout(inspectTask, DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS.toMillis(), TimeUnit.MILLISECONDS);
            inspect();
        }
    };

    public static void inspect() {
        poolMap.forEach((aClass, objectPool) -> {
            try {
                objectPool.evict();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    static class PoolObject<T extends IPool> implements PooledObjectFactory<T> {

        Class<T> cls;

        public PoolObject(Class<T> cls) {
            this.cls = cls;
        }

        //在借用一个对象的时候调用，则可以在此重置其内部状态，那么返回的对象就像新的一样，例如这里重置电量
        @Override
        public void activateObject(PooledObject<T> p) throws Exception {

        }

        //销毁一个对象，除了很容易想到的闲置过长时间被清理掉了导致需要销毁之外，还有如果进行了第三个方法且返回了 false ，那么也是需要销毁的。
        @Override
        public void destroyObject(PooledObject<T> p) throws Exception {
            try {
                T t = p.getObject();
                Method method = MethodUtils.getMatchingAccessibleMethod(t.getClass(), "close");
                if (method != null) {
                    method.invoke(t);
                }
            } catch (Exception e) {
            }
        }

        //用于对象的新建，一般是 new 出来之后包装一下。而什么时候需要新建呢，根据策略不同则时机不同。例如在没有闲置资源对象，且已存在的资源数不超过所设置的最大资源时新建。
        @Override
        public PooledObject<T> makeObject() throws Exception {
            T entity = cls.newInstance();
            return new DefaultPooledObject<>(entity);
        }

        //对应 activateObject 方法，是在归还一个对象的时候调用，注意不应与activateObject方法有业务逻辑上的冲突
        @Override
        public void passivateObject(PooledObject<T> p) throws Exception {
            T entity = p.getObject();
            entity.reset();
        }

        //检验这个对象是否还有有效，借出和归还时，以及内置后台线程检测闲置情况时，可以通过验证可以去除一些不符合业务逻辑的资源对象。默认这个方法是不被调用的，要开启则需要在PoolConfig中设置setTestOnBorrow , setTestOnReturn , setTestWhileIdle等属性。
        @Override
        public boolean validateObject(PooledObject<T> p) {
            return true;
        }
    }

    private static  <T> GenericObjectPoolConfig<T> createConfig() {
        GenericObjectPoolConfig<T> config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(DEFAULT_MAX_TOTAL);
        config.setMaxIdle(DEFAULT_MAX_IDLE);
        config.setMinIdle(DEFAULT_MIN_IDLE);
//        config.setTimeBetweenEvictionRuns(DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS);
        return config;
    }

}


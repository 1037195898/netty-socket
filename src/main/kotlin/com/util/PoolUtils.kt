package com.util

import com.interfaces.IPool
import io.netty.util.Timeout
import io.netty.util.TimerTask
import org.apache.commons.pool2.PooledObject
import org.apache.commons.pool2.PooledObjectFactory
import org.apache.commons.pool2.impl.*
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.reflect.full.functions

/**
 * 对象池
 */
object PoolUtils {

    private val poolMap = ConcurrentHashMap<Class<*>, GenericObjectPool<out IPool<*>>>()

    /** 对象池中最大存在数量 默认-1是int最大值  */
    @JvmField
    var DEFAULT_MAX_TOTAL: Int = GenericKeyedObjectPoolConfig.DEFAULT_MAX_TOTAL
    var DEFAULT_MAX_IDLE: Int = 100
    var DEFAULT_MIN_IDLE: Int = 1

    /** 开启检查空闲和遗弃  */
    var OPEN_INSPECT_IDEA_REMOVE: Boolean = true

    /** 启动默认空闲和遗弃清理线程 默认5分半 要比超时场  */
    @JvmField
    var DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS: Duration = Duration.ofSeconds(330)

    /** 设置为归还超时要被遗弃的秒数 默认5分钟  */
    @JvmField
    var REMOVE_ABANDONED_TIMEOUT: Duration = Duration.ofMinutes(5)
    var abandonedConfig: AbandonedConfig? = null

    /**
     * 获取一个对象实例
     * @param cls
     * @return
     * @param <T>
     * @throws Exception 会超时报错 超时时间是  PoolUtils.REMOVE_ABANDONED_TIMEOUT
    </T> */
    fun <T : IPool<*>> get(cls: Class<T>): T {
        return getObject(cls, REMOVE_ABANDONED_TIMEOUT)
    }

    /**
     * 获取一个对象实例
     * 等效于 borrowObject(getMaxWaitDuration())。从此池中借用实例。
     * 默认是无限等待
     */
    @JvmStatic
    fun <T : IPool<*>> getObject(cls: Class<T>): T {
        return getPool(cls).borrowObject()
    }

    /**
     * 获取一个对象实例
     * @param borrowMaxWaitDuration 等待对象可用的时间
     */
    fun <T : IPool<*>> getObject(cls: Class<T>, borrowMaxWaitDuration: Duration): T {
        return getPool(cls).borrowObject(borrowMaxWaitDuration)
    }

    /**
     * 获取一个对象实例
     * @param cls
     * @param borrowMaxWaitMillis 等待对象可用的时间（毫秒）
     */
    fun <T : IPool<*>> getObject(cls: Class<T>, borrowMaxWaitMillis: Long): T {
        return getPool(cls).borrowObject(borrowMaxWaitMillis)
    }

    /** 归还对象  */
    fun <T : IPool<*>> returnObject(pool: T?) {
        pool?.let { getPool(it.javaClass).returnObject(it) }
    }

    /** 在池的生存期内为此池创建的对象总数。  */
    fun getCreatedCount(cls: Class<IPool<*>>): Long {
        return getPool(cls).createdCount
    }

    /** 在池的生存期内返回到此池的对象总数。这排除了多次返回同一对象的尝试。  */
    fun getReturnedCount(cls: Class<IPool<*>>): Long {
        return getPool(cls).returnedCount
    }

    /** 此池在其生存期内销毁的对象总数。  */
    fun getDestroyedCount(cls: Class<IPool<*>>): Long {
        return getPool(cls).destroyedCount
    }

    /** 在池的生存期内，由于在borrowObject（）期间验证失败而被池销毁的对象总数。。  */
    fun getDestroyedByBorrowValidationCount(cls: Class<IPool<*>>): Long {
        return getPool(cls).destroyedByBorrowValidationCount
    }

    /** 在池的生存期内成功从此池借用的对象总数。  */
    fun getBorrowedCount(cls: Class<IPool<*>>): Long {
        return getPool(cls).borrowedCount
    }

    /** 获取此池中当前空闲的实例数  */
    @JvmStatic
    fun <T : IPool<*>> getNumIdle(cls: Class<T>): Long {
        return getPool(cls).numIdle.toLong()
    }

    /** 获取当前从此池借用的实例数。  */
    fun getNumActive(cls: Class<IPool<*>>): Long {
        return getPool(cls).numActive.toLong()
    }

    /** 获取指定对象的线程池  */
    @JvmStatic
    fun <T : IPool<*>> getPool(cls: Class<T>): GenericObjectPool<T> {
        return poolMap.getOrPut(cls) {
            GenericObjectPool(PoolObject(cls), createConfig()).also {
                if (abandonedConfig == null) {
                    abandonedConfig = AbandonedConfig().apply {
                        // 开启池中已经饱和执行遗弃
                        this.removeAbandonedOnBorrow = true
                        // 活跃的对象也可以执行遗弃
                        this.removeAbandonedOnMaintenance = true
                        // 300秒未被回收的  遗弃掉
                        this.setRemoveAbandonedTimeout(REMOVE_ABANDONED_TIMEOUT)
                    }

                }
                if (OPEN_INSPECT_IDEA_REMOVE) {
                    TimerUtils.newTimeout(
                        inspectTask,
                        DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS.toMillis(),
                        TimeUnit.MILLISECONDS
                    )
                }
                it.setAbandonedConfig(abandonedConfig)
            }
        } as GenericObjectPool<T>
    }

    var inspectTask = object : TimerTask {
        override fun run(timeout: Timeout) {
            TimerUtils.newTimeout(this, DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS.toMillis(), TimeUnit.MILLISECONDS)
            inspect()
        }
    }

    fun inspect() {
        poolMap.forEach {
            it.value.evict()
        }
    }

    private fun <T> createConfig() = GenericObjectPoolConfig<T>().apply {
        this.maxTotal = DEFAULT_MAX_TOTAL
        this.maxIdle = DEFAULT_MAX_IDLE
        this.minIdle = DEFAULT_MIN_IDLE
//        this.timeBetweenEvictionRuns = DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS
    }


    internal class PoolObject<T : IPool<*>>(private var cls: Class<T>) : PooledObjectFactory<T> {
        /**
         * 在借用一个对象的时候调用，则可以在此重置其内部状态，那么返回的对象就像新的一样，例如这里重置电量
         */
        override fun activateObject(p: PooledObject<T>) {
            LoggerFactory.getLogger(PoolObject::class.java).trace("获取一个对象 激活")
        }

        /**
         * 销毁一个对象，除了很容易想到的闲置过长时间被清理掉了导致需要销毁之外，还有如果进行了第三个方法且返回了 false ，那么也是需要销毁的。
         */
        override fun destroyObject(p: PooledObject<T>) {
            runCatching {
                p.getObject().let {
                    when(it) {
                        is Closeable -> it.close()
                        else -> {
                            it::class.functions.find { method -> method.name == "close" }?.run {
                                LoggerFactory.getLogger(PoolObject::class.java).trace("销毁一个")
                                call(it)
                            }
                        }
                    }
//                    MethodUtils.getMatchingAccessibleMethod(it.javaClass, "close")?.invoke(it)
                }
            }
        }

        /**
         * 用于对象的新建，一般是 new 出来之后包装一下。而什么时候需要新建呢，根据策略不同则时机不同。例如在没有闲置资源对象，且已存在的资源数不超过所设置的最大资源时新建。
         */
        override fun makeObject(): PooledObject<T> {
            val entity = cls.newInstance()
            LoggerFactory.getLogger(PoolObject::class.java).trace("新建")
            return DefaultPooledObject(entity)
        }

        /**
         * 对应 activateObject 方法，是在归还一个对象的时候调用，注意不应与activateObject方法有业务逻辑上的冲突
         */
        override fun passivateObject(p: PooledObject<T>) {
            val entity = p.getObject()
            entity.reset()
            LoggerFactory.getLogger(PoolObject::class.java).trace("归还")
        }

        /**
         * 检验这个对象是否还有有效，借出和归还时，以及内置后台线程检测闲置情况时，可以通过验证可以去除一些不符合业务逻辑的资源对象。默认这个方法是不被调用的，要开启则需要在PoolConfig中设置setTestOnBorrow , setTestOnReturn , setTestWhileIdle等属性。
         */
        override fun validateObject(p: PooledObject<T>): Boolean {
            return true
        }

    }
}


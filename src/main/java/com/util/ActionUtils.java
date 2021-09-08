package com.util;

import com.socket.ActionData;
import com.socket.ActionHandler;
import com.socket.SessionListener;
import io.netty.channel.ChannelHandlerContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class ActionUtils {

    private Map<Integer, ActionHandler<?>> actionMapping = new ConcurrentHashMap<>();
    private Set<SessionListener> listeners = new CopyOnWriteArraySet<>();
    /**
     * 已经被出现闲置状态的isSession
     */
    private Map<String, Integer> iosIdle = new HashMap<>();

    private static volatile ActionUtils actionUtils;
    public static ActionUtils getInst() {
        if (actionUtils == null) {
            synchronized (ActionUtils.class) {
                if (actionUtils == null) {
                    actionUtils = new ActionUtils();
                }
            }
        }
        return actionUtils;
    }


    public void addSessionListener(SessionListener sessionListener) {
        listeners.add(sessionListener);
    }

    /**
     * 注册动作
     *
     * @param action  动作
     * @param handler 处理器
     */
    public void registerAction(int action, ActionHandler<?> handler) {
        if (actionMapping.containsKey(action)) {
            throw new RuntimeException("动作处理器[" + action + "]已经注册");
        }
        actionMapping.put(action, handler);
        if (handler instanceof SessionListener) {
            listeners.add((SessionListener) handler);
        }

    }

    /**
     * 删除动作
     *
     * @param action 动作
     */
    public void removeAction(int action) {
        ActionHandler<?> handler = actionMapping.remove(action);
        if (handler instanceof SessionListener) {
            listeners.remove(handler);
        }
    }

    /**
     * 获取动作
     *
     * @param action 动作
     * @return
     */
    public ActionHandler<?> getAction(int action) {
        return actionMapping.get(action);
    }

    /**
     * 执行一个  监听器
     *
     * @param data    数据
     * @param session 通信ioSession
     * @param message 未被解析的数据
     * @throws Exception
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void executeActionMapping(ActionData data, ChannelHandlerContext session, Object message) throws Exception {
        int action = data.getAction();
//		log.info("检测处理器["+action+"]");
        if (!actionMapping.containsKey(action)) {
//			throw new RuntimeException("动作处理器["+action+"]未注册");
            for (SessionListener listener : listeners) {
                listener.notRegAction(session, message);
            }
            return;
        }
        actionMapping.get(action).execute(data, session);
    }

    public Map<Integer, ActionHandler<?>> getActionMapping() {
        return actionMapping;
    }

    public Set<SessionListener> getListeners() {
        return listeners;
    }

    public Map<String, Integer> getIosIdle() {
        return iosIdle;
    }

}

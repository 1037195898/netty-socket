package com.socket

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet

/**
 * 事件管理器
 */
class ActionEventManager {

    private val actionMapping = ConcurrentHashMap<Int, ActionHandler<Any>>()
    private val listeners = CopyOnWriteArraySet<SessionListener<Any>>()

    /**
     * 已经被出现闲置状态的isSession
     */
    @JvmField
    val iosIdle = mutableMapOf<String, Int>()

    fun <T : Any> addSessionListener(sessionListener: SessionListener<T>) {
        listeners.add(sessionListener as SessionListener<Any>)
    }

    /**
     * 注册动作
     *
     * @param action  动作
     * @param handler 处理器
     */
    fun registerAction(action: Int, handler: ActionHandler<Any>) {
        if (actionMapping.containsKey(action)) {
            throw RuntimeException("动作处理器[$action]已经注册")
        }
        actionMapping[action] = handler
        if (handler is SessionListener<*>) {
            listeners.add(handler as SessionListener<Any>)
        }
    }

    /**
     * 删除动作
     *
     * @param action 动作
     */
    fun removeAction(action: Int) {
        val handler = actionMapping.remove(action)!!
        if (handler is SessionListener<*>) {
            listeners.remove(handler)
        }
    }

    /**
     * 获取动作
     *
     * @param action 动作
     * @return
     */
    fun getAction(action: Int): ActionHandler<Any>? {
        return actionMapping[action]
    }

    /**
     * 执行一个  监听器
     *
     * @param data    数据
     * @param session 通信ioSession
     * @param message 未被解析的数据
     * @param ignoreNotReg 是否忽略未注册事件发送监听
     * @throws Exception
     */
    fun executeActionMapping(data: ActionData<Any>, session: IoSession, message: ByteArray?, ignoreNotReg: Boolean) {
        val action = data.action
        //		log.info("检测处理器["+action+"]");
        if (!actionMapping.containsKey(action)) {
//			throw new RuntimeException("动作处理器["+action+"]未注册");
            if (!ignoreNotReg) {
                listeners.forEach {
                    it.notRegAction(session, data)
                }
            }
            return
        }
        actionMapping[action]?.execute(data, session)
    }

    fun getActionMapping(): Map<Int, ActionHandler<Any>> {
        return actionMapping
    }

    fun getListeners(): Set<SessionListener<Any>> {
        return listeners
    }
}

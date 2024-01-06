package com.socket

interface ActionHandler<T> {
    /**
     * 收到消息时调用。
     * @param actionData
     * @param session
     */
    fun execute(actionData: ActionData<T>, session: IoSession)
}

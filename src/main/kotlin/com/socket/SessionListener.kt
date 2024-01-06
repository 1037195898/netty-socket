package com.socket

import io.netty.handler.timeout.IdleState

interface SessionListener<in T> {
    /**
     * 当有新的连接建立的时候，该方法被调用。
     * @param session
     */
    fun sessionCreated(session: IoSession)

    /**
     * 当连接被关闭的时候，此方法被调用。
     * @param session
     */
    fun sessionClosed(session: IoSession)

    /**
     * 调用任何异常是由用户执行或由 MINA IoHandler 抛出。如果原因是IOException实例，MINA会自动关闭连接。
     * @param session
     * @param cause
     */
    fun exceptionCaught(session: IoSession, cause: Throwable)

    /**
     * 当连接变成闲置状态的时候，此方法被调用。
     * @param session
     * @param status
     */
    fun sessionIdle(session: IoSession, status: IdleState)

    /**
     * 当消息被成功发送出去的时候，此方法被调用。
     * @param message
     */
    fun messageSent(session: IoSession, message: T)

    /**
     * 当接收到新的消息的时候，此方法被调用。
     * @param session
     * @param message
     */
    fun messageReceived(session: IoSession, message: T)

    /**
     * 未注册的action
     * @param session
     * @param message
     */
    fun notRegAction(session: IoSession, message: T?)

    /**
     * websocket 握手成功
     * @param session
     */
    fun handshakeComplete(session: IoSession) {}
}

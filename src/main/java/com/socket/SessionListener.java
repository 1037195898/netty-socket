package com.socket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;

public interface SessionListener<T> {

	/**
	 * 当有新的连接建立的时候，该方法被调用。
	 * @param session
	 */
	void sessionCreated(IoSession session);
	
	/**
	 * 当连接被关闭的时候，此方法被调用。
	 * @param session
	 */
	void sessionClosed(IoSession session);
	
	/**
	 * 调用任何异常是由用户执行或由 MINA IoHandler 抛出。如果原因是IOException实例，MINA会自动关闭连接。
	 * @param session
	 * @param cause
	 */
	void exceptionCaught(IoSession session, Throwable cause);

	/**
	 * 当连接变成闲置状态的时候，此方法被调用。
	 * @param session
	 * @param status
	 */
	void sessionIdle(IoSession session, IdleState status);

	/**
	 * 当消息被成功发送出去的时候，此方法被调用。
	 * @param message
	 */
	void messageSent(IoSession session,T message);

	/**
	 * 当接收到新的消息的时候，此方法被调用。
	 * @param session
	 * @param message
	 */
	void messageReceived(IoSession session, T message);

	/**
	 * 未注册的action
	 * @param session
	 * @param message
	 */
	void notRegAction(IoSession session, T message);

	/**
	 * websocket 握手成功
	 * @param session
	 */
	void handshakeComplete(IoSession session);

}

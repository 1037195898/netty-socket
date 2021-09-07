package com.socket;

import io.netty.channel.ChannelHandlerContext;

public abstract interface ActionHandler<T>{
	
	/**
	 * 收到消息时调用。
	 * @param actionData
	 * @param session
	 */
	abstract void execute (ActionData<T> actionData, ChannelHandlerContext session);
	
}

package com.socket;

public abstract interface ActionHandler<T>{
	
	/**
	 * 收到消息时调用。
	 * @param actionData
	 * @param session
	 */
	abstract void execute (ActionData<T> actionData, IoSession session);
	
}

package com.util

object SocketUtils {
    /** 默认websocket通信格式 文本格式  */
    @JvmField
    var webSocketType: SocketType = SocketType.TEXT_WEB_SOCKET_FRAME

    /** 默认空闲事件命令  */
    @JvmField
    var DEFAULT_IDLE_ACTION: Int = 1
}

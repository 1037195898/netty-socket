package com.ioByte

import com.socket.ActionData
import com.socket.ActionHandler
import com.socket.IoSession

class ByteChannelAdapter : ActionHandler<Any> {

    override fun execute(actionData: ActionData<Any>, session: IoSession) {
        println("${actionData.action}: ${actionData.buf?.decodeToString()}")
    }
}

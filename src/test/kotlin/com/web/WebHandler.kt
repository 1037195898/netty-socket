package com.web

import com.annotation.SocketAction
import com.entity.Input
import com.socket.ActionData
import com.socket.ActionHandler
import com.socket.IoSession
import org.slf4j.LoggerFactory
import java.io.IOException

class WebHandler : ActionHandler<Any> {

    override fun execute(actionData: ActionData<Any>, session: IoSession) {
        val input = Input(actionData.buf!!)
        try {
            println("channelRead0: ${input.readUTF()}")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @SocketAction(1)
    private fun heartbeat() {
        println("心跳1")
    }

    @SocketAction(1)
    fun heartbeat(session: IoSession?) {
        println("心跳2 $session")
    }

    @SocketAction(1)
    fun heartbeat(actionData: ActionData<Any>, session: IoSession?) {
        println("心跳3 $actionData $session")
    }

    @SocketAction(1)
    private fun heartbeat(actionData: ActionData<Any>) {
        println("心跳4 $actionData")
    }

}
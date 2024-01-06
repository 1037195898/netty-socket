package com.ioByte

import com.adapter.MessageAdapter
import com.initializer.ByteChannelHandler
import com.parse.MessageDecoder
import com.parse.MessageEncoder
import com.parse.WebSocketDecoder
import com.parse.WebSocketEncoder
import com.socket.ActionData
import com.socket.ClientAcceptor
import com.socket.IoSession
import com.socket.SessionListener
import com.util.PoolUtils
import io.netty.handler.timeout.IdleState
import java.util.*

class Client : SessionListener<ActionData<*>> {
    init {
        val clientAcceptor = ClientAcceptor()
        clientAcceptor.addListener(this)
        clientAcceptor.handler(
            ByteChannelHandler(
                MessageAdapter(clientAcceptor.actionEventManager)
            )
        )
        clientAcceptor.registerAction(ByteChannelAdapter(), 100)
        clientAcceptor.connect("0.0.0.0", 9099)

        try {
            println("客户端启动")
            //测试输入
            while (true) {
                val scanner = Scanner(System.`in`)
                println("请输入：")
                val msg = scanner.nextLine()
                val action = PoolUtils.getObject(ActionData::class.java).apply {
                    this.action = 100
                    this.buf = msg.toByteArray()
                }
                clientAcceptor.writeAndFlush(action)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
        }
    }

    override fun sessionCreated(session: IoSession) {
    }

    override fun sessionClosed(session: IoSession) {
    }

    override fun exceptionCaught(session: IoSession, cause: Throwable) {
    }

    override fun sessionIdle(session: IoSession, status: IdleState) {
    }

    override fun messageSent(session: IoSession, message: ActionData<*>) {
        println("send $message")
    }

    override fun messageReceived(session: IoSession, message: ActionData<*>) {
        println("messageReceived:$message")
    }

    override fun notRegAction(session: IoSession, message: ActionData<*>?) {
    }

    override fun handshakeComplete(session: IoSession) {
    }

}

fun main() {
    System.setProperty("rootDir", "D:\\WorkSpace\\Idea\\Java\\NettySocket/log/client")
    Client()
}
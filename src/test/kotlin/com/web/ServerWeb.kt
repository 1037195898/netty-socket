package com.web

import com.adapter.MessageAdapter
import com.initializer.WebSocketChannelInitializer
import com.parse.WebSocketDecoder
import com.parse.WebSocketEncoder
import com.socket.ActionData
import com.socket.IoSession
import com.socket.ServerAcceptor
import com.socket.SessionListener
import com.util.ActionUtils
import com.util.SocketType
import com.util.SocketUtils
import io.netty.handler.timeout.IdleState
import io.netty.handler.timeout.IdleStateHandler
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

class ServerWeb : SessionListener<ActionData<*>> {

    private val logger = LoggerFactory.getLogger(javaClass)
    init {
        logger.info("开始")

        SocketUtils.webSocketType = SocketType.BINARY_WEB_SOCKET_FRAME
        val serverAcceptor = ServerAcceptor()
        serverAcceptor.addListener(this)
        serverAcceptor.handler(
            WebSocketChannelInitializer(
                WebSocketDecoder.inst(true),
                WebSocketEncoder.inst(true),
                MessageAdapter(serverAcceptor.actionEventManager),
                IdleStateHandler(5, 5, 10, TimeUnit.SECONDS)
            )
        )

        val handler = WebHandler()
        ActionUtils.addAction(handler)
        serverAcceptor.registerAction(handler, 100)
        serverAcceptor.bind(9099)
        println("测试服务器开启!按任意键+回车关闭")
    }


    override fun sessionCreated(session: IoSession) {
        println("连接一个=${session.channel().id()} , ${session.channel().id().asLongText()}")
    }


    override fun sessionClosed(session: IoSession) {
        println("断开一个 ${session.channel().id().asLongText()}")
    }


    override fun exceptionCaught(session: IoSession, cause: Throwable) {
        session.channel().closeFuture()
        println("意外断开一个 ${session.channel().id().asLongText()}")
        //        cause.printStackTrace();
    }


    override fun sessionIdle(session: IoSession, status: IdleState) {
        if (status == IdleState.READER_IDLE) {
            println("空闲 ${status.name}")
        }
    }


    override fun messageSent(session: IoSession, message: ActionData<*>) {
    }


    override fun messageReceived(session: IoSession, message: ActionData<*>) {
        logger.info(" ${Thread.currentThread().name} | messageReceived: $message")
    }


    override fun notRegAction(session: IoSession, message: ActionData<*>?) {
        if (message is ActionData<*>) {
            if (message.action == -100) {
                logger.debug("notRegAction: ${message.buf?.decodeToString()}")
            } else {
                logger.debug("notRegAction:{}", message)
            }
        }
    }

}

fun main() {
    System.setProperty("rootDir", "D:\\WorkSpace\\Idea\\Java\\NettySocket/webServer")
    ServerWeb()
}
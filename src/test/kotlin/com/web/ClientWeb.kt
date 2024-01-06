package com.web

import com.adapter.MessageAdapter
import com.entity.Output
import com.initializer.WebSocketChannelInitializer
import com.parse.WebSocketDecoder
import com.parse.WebSocketEncoder
import com.socket.ActionData
import com.socket.ClientAcceptor
import com.socket.IoSession
import com.socket.SessionListener
import com.util.PoolUtils
import com.util.SocketType
import com.util.SocketUtils
import io.netty.handler.ssl.SslContext
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.SslHandler
import io.netty.handler.ssl.util.InsecureTrustManagerFactory
import io.netty.handler.timeout.IdleState
import io.netty.handler.timeout.IdleStateHandler
import org.slf4j.LoggerFactory
import java.net.URI
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

class ClientWeb : SessionListener<ActionData<*>> {

    private val clientAcceptor = ClientAcceptor()

    init {

        val uri = URI.create("ws://localhost:9099/ws")

        SocketUtils.webSocketType = SocketType.BINARY_WEB_SOCKET_FRAME
        clientAcceptor.addListener(this)
        clientAcceptor.handler(
            WebSocketChannelInitializer(
                uri,
                { channel, pipeline ->
                    try {
                        val isSsl = uri.scheme.equals("wss")
                        var port = uri.port
                        if (port == -1) {
                            port = if (isSsl) {
                                443
                            } else {
                                80
                            }
                        }
                        if (isSsl) {
                            val t: SslContext =
                                SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build()
                            val c: SslHandler = t.newHandler(channel.alloc(), uri.host, port)
                            pipeline.addFirst(c)
                        }
                    } catch (e: Exception) {
                        throw RuntimeException(e)
                    }
                },
                WebSocketDecoder.inst(true),
                WebSocketEncoder.inst(true),
                MessageAdapter(clientAcceptor.actionEventManager),
                IdleStateHandler(3, 3, 5, TimeUnit.SECONDS)
            )
        )
        clientAcceptor.registerAction(WebHandler(), 100)
        try {
            clientAcceptor.connect(uri)
            println("客户端启动")
            //测试输入
            while (true) {
                val scanner = Scanner(System.`in`)
                println("请输入：")
                val msg = scanner.nextLine()
                val gameOutput = PoolUtils.getObject(Output::class.java)
                gameOutput.writeUTF(msg)
                val action = PoolUtils.getObject(ActionData::class.java).apply {
                    this.action = 100
                    this.buf = gameOutput.toByteArray()
                }
                clientAcceptor.writeAndFlush(action)
                //                clientAcceptor.writeAndFlush(new TextWebSocketFrame(msg));
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
        }
    }


    override fun sessionCreated(session: IoSession) {
    }


    override fun sessionClosed(session: IoSession) {
        println("断开了")
        exitProcess(0)
    }


    override fun exceptionCaught(session: IoSession, cause: Throwable) {
    }


    override fun sessionIdle(session: IoSession, status: IdleState) {
        println("sessionIdle")
        session.writeFlush(PoolUtils.getObject(ActionData::class.java).also { it.action = SocketUtils.DEFAULT_IDLE_ACTION })
    }


    override fun messageSent(session: IoSession, message: ActionData<*>) {
    }


    override fun messageReceived(session: IoSession, message: ActionData<*>) {
        println(Thread.currentThread().name + "|messageReceived:" + message)
    }


    override fun notRegAction(session: IoSession, message: ActionData<*>?) {
        if (message is ActionData<*>) {
            if (message.action == -100) {
                LoggerFactory.getLogger(javaClass).debug("notRegAction: ${message.buf?.decodeToString()}")
            } else {
                LoggerFactory.getLogger(javaClass).debug("notRegAction:{}", message)
            }
        }
    }


    override fun handshakeComplete(session: IoSession) {
        LoggerFactory.getLogger(javaClass).info("handshakeComplete")
    }

}

fun main() {
    System.setProperty("rootDir", "D:\\WorkSpace\\Idea\\Java\\NettySocket/webClient")
    ClientWeb()
}
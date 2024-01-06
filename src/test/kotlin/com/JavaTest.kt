package com

import com.socket.ActionData
import com.util.ActionUtils
import com.util.IOUtils
import com.util.PoolUtils
import com.util.ZlibUtil
import com.web.WebHandler
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalStdlibApi::class, ExperimentalEncodingApi::class)
class JavaTest {
    @BeforeTest
    fun before() {
        System.setProperty("rootDir", "E:\\WorkSpace\\Idea\\Java\\NettySocket/webClient")
    }

    @Test
    fun test() {
        val handler = WebHandler()
        ActionUtils.addAction(handler)
        ActionUtils.run(
            1,
            PoolUtils.getObject(ActionData::class.java).also { it.action = 1 },
//            IoSession(object : AbstractChannelHandlerContext {
//
//                                                     }, null)
        )
    }

    @Test
    fun aesTest() {
        for (i in 0..4) {
            Thread {
                for (j in 0..1) {
                    println(IOUtils.aes)
                }
            }.start()
        }
        TimeUnit.SECONDS.sleep(5)
    }

    @Test
    fun zlibTest() {
        for (j in 0..1) {
            val str = "welcome toto welcome toto $j"
            val bytes = str.toByteArray(StandardCharsets.UTF_8)
            println(bytes.size)
            val byte2 = ZlibUtil.compress(bytes)
            println(byte2.size)

            val byte3 = ZlibUtil.decompress(byte2)
            println(String(byte3))
        }
    }

    @Test
    fun haxTest() {
        println("e68891e59388e59388".hexToByteArray().decodeToString())

        println(Base64.encode("我来了".toByteArray()))

        println(Base64.decode("5oiR5p2l5LqG").decodeToString())


        var start: Byte = 0
        val length = 200
        val step: Byte = 5
        var start1 = start
        var step1 = step
        var result = ByteArray(length)
        for (i in 0 until length) {
            result[i] = start1
            val next = start1.toInt() + step1.toInt()
            if (next < Byte.MIN_VALUE || next > Byte.MAX_VALUE) {
                // 正序
                if (step1 > 0) {
                    start1 = Byte.MAX_VALUE
                    step1 = 0
                } else {
                    start1 = Byte.MIN_VALUE
                    step1 = 0
                }
            } else {
                start1 = next.toByte()
            }
        }

        println(result.toHexString())

        println("------------------")
        start = 0
        result = ByteArray(length) { (start + (it * step / length)).toByte() }
        println(result.toHexString())
    }

    @Test
    fun zlibThreadTest() {
        for (i in 0..4) {
            val finalI = i
            Thread {
                for (j in 0..1) {
                    val str = "welcome toto welcome toto " + finalI + "_" + j
                    val bytes = str.toByteArray(StandardCharsets.UTF_8)
                    println(bytes.size)
                    try {
                        val byte2 = ZlibUtil.compress(bytes)
                        println(byte2.size)

                        val byte3 = ZlibUtil.decompress(byte2)
                        println(String(byte3))
                    } catch (e: Exception) {
                        throw RuntimeException(e)
                    }
                }
            }.start()
        }
        TimeUnit.SECONDS.sleep(5)
    }
}

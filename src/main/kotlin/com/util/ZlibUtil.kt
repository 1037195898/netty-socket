package com.util

import java.io.ByteArrayOutputStream
import java.util.zip.Deflater
import java.util.zip.Inflater

object ZlibUtil {
    /**
     * 用zlib压缩
     * @param data
     * @throws Exception
     */
    @JvmStatic
	@Throws(Exception::class)
    fun compress(data: ByteArray): ByteArray {
        val output: ByteArray
        val deflater = Deflater()
        deflater.reset()
        deflater.setInput(data)
        deflater.finish()
        val bos = ByteArrayOutputStream(data.size)
        try {
            val buf = ByteArray(1024)
            while (!deflater.finished()) {
                val i = deflater.deflate(buf)
                bos.write(buf, 0, i)
            }
            output = bos.toByteArray()
        } finally {
            bos.close()
        }
        deflater.end()
        return output
    }

    /**
     * 解压缩
     * @param data 需要解压缩的字节数组
     * @throws Exception
     */
    @JvmStatic
	@Throws(Exception::class)
    fun decompress(data: ByteArray): ByteArray {
        val output: ByteArray
        val inflater = Inflater()
        inflater.reset()
        inflater.setInput(data)
        val o = ByteArrayOutputStream(data.size)
        try {
            val buf = ByteArray(1024)
            while (!inflater.finished()) {
                val i = inflater.inflate(buf)
                o.write(buf, 0, i)
            }
            output = o.toByteArray()
        } finally {
            o.close()
        }
        inflater.end()
        return output
    }
}

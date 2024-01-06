package com.entity

import java.io.ByteArrayInputStream
import java.io.Closeable
import java.io.DataInputStream

data class Input(
    private var inputStream: DataInputStream? = null,
) : Closeable {

    constructor(buf: ByteArray) : this(buf.inputStream())
    constructor(byteArray: ByteArrayInputStream) : this(DataInputStream(byteArray))

    fun reset() {
        inputStream?.reset()
    }

    override fun close() {
        inputStream?.close()
        inputStream = null
    }

    fun available(): Int {
        return inputStream?.available() ?: 0
    }

    fun readInt(default: Int = 0): Int {
        return inputStream?.readInt() ?: default
    }

    fun readShort(default: Short = 0): Short {
        return inputStream?.readShort() ?: default
    }

    fun readUTF(default: String = ""): String {
        return inputStream?.readUTF() ?: default
    }

    fun readBoolean(default: Boolean = false): Boolean {
        return inputStream?.readBoolean() ?: default
    }

    fun readLong(default: Long = 0): Long {
        return inputStream?.readLong() ?: default
    }

    fun readFloat(default: Float = 0f): Float {
        return inputStream?.readFloat() ?: default
    }

    fun readDouble(default: Double = 0.0): Double {
        return inputStream?.readDouble() ?: default
    }

    /**
     * 根据传入的byte长度 读取字节
     */
    fun read(b: ByteArray): Int {
        return inputStream?.read(b) ?: 0
    }

    fun read(b: ByteArray, off: Int, len: Int): Int {
        return inputStream?.read(b, off, len) ?: 0
    }

}

package com.entity

import com.interfaces.IPool
import com.interfaces.IWrite
import com.util.PoolUtils
import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.io.DataOutputStream
import java.time.Duration

data class Output(val byteArray: ByteArrayOutputStream = ByteArrayOutputStream()) : IPool<Output>, Closeable {

    private var outputStream: DataOutputStream? = DataOutputStream(byteArray)

    /**
     * 创建一个新分配的 byte 数组。其大小是此输出流的当前大小，并且缓冲区的有效内容已复制到该数组中。
     *
     * @return 以 byte 数组的形式返回此输出流的当前内容。
     */
    fun toByteArray(): ByteArray = byteArray.toByteArray()

    override fun reset(): Output {
        byteArray.reset()
        return this
    }

    /**
     * 写入list数据 　只能存 int long utf  boolean
     * @param list
     */
    fun <T> writeList(list: List<T>): Output {
        val len = list.size
        this.writeInt(len)
        for (i in 0 until len) {
            when (val obj = list[i]) {
                is Long -> this.writeInt(1).writeLong(obj.toLong())
                is Int -> this.writeInt(2).writeInt(obj.toInt())
                is Boolean -> this.writeInt(3).writeBoolean(obj as Boolean)
                else -> this.writeInt(0).writeUTF(obj.toString())
            }
        }
        return this
    }

    fun writeUTF(s: String?): Output {
        outputStream?.writeUTF(s ?: "")
        return this
    }

    fun writeInt(v: Int?): Output {
        outputStream?.writeInt(v ?: 0)
        return this
    }

    fun writeShort(v: Int?): Output {
        outputStream?.writeShort(v ?: 0)
        return this
    }

    fun writeFloat(v: Float?): Output {
        outputStream?.writeFloat(v ?: 0f)
        return this
    }

    fun writeDouble(v: Double?): Output {
        outputStream?.writeDouble(v ?: 0.0)
        return this
    }

    fun writeLong(v: Long?): Output {
        outputStream?.writeLong(v ?: 0L)
        return this
    }

    fun writeByte(v: Int): Output {
        outputStream?.writeByte(v)
        return this
    }

    fun writeBoolean(v: Boolean?): Output {
        outputStream?.writeBoolean(v ?: false)
        return this
    }

    fun write(bytes: ByteArray, offset: Int, length: Int): Output {
        outputStream?.write(bytes, offset, length)
        return this
    }

    /**
     * 写入实体
     * @param write
     * @return
     */
    fun writeEntity(write: IWrite): Output {
        write.write(this)
        return this
    }

    override fun close() {
        outputStream?.close()
        outputStream = null
    }

    /**
     * 自动判断类型写入
     *
     * @param arge 自动判断类型包括(Boolean、Integer、Long、byte[]、其余全是String)
     * @return
     */
    fun <T> write(vararg arge: T): Output {
        var obj: T
        var b: ByteArray
        for (i in arge.indices) {
            obj = arge[i]
            if (obj is Boolean) {
                writeBoolean(obj as Boolean?)
            } else if (obj is Int) {
                writeInt(obj as Int?)
            } else if (obj is Long) {
                writeLong(obj as Long?)
            } else if (obj is ByteArray) {
                b = obj
                write(b, 0, b.size)
            } else if (obj is List<*>) {
                writeList(obj)
            } else {
                writeUTF(obj?.toString() ?: "")
            }
        }
        return this
    }

    /** 生成数据并回收  */
    fun toByteReturn(): ByteArray {
        return this.toByteArray().also {
            PoolUtils.returnObject(this)
        }
    }


    companion object {
        fun get(): Output {
            return PoolUtils.getObject(Output::class.java)
        }

        fun get(borrowMaxWaitMillis: Long): Output {
            return PoolUtils.getObject(Output::class.java, borrowMaxWaitMillis)
        }

        fun get(borrowMaxWaitDuration: Duration): Output {
            return PoolUtils.getObject(Output::class.java, borrowMaxWaitDuration)
        }
    }
}

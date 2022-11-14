package com.entity;

import com.interfaces.IPool;
import com.interfaces.IWrite;
import com.util.PoolUtils;
import lombok.Getter;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.List;

@Getter
public class GameOutput implements IPool<GameOutput> {

    private ByteArrayOutputStream byteArrayOutputStream;
    private DataOutputStream outputStream;

    public GameOutput() {
        this(new ByteArrayOutputStream());
    }

    public GameOutput(ByteArrayOutputStream outputStream) {
        this.byteArrayOutputStream = outputStream;
        this.outputStream = new DataOutputStream(byteArrayOutputStream);
    }

    /**
     * 创建一个新分配的 byte 数组。其大小是此输出流的当前大小，并且缓冲区的有效内容已复制到该数组中。
     *
     * @return 以 byte 数组的形式返回此输出流的当前内容。
     */
    public byte[] toByteArray() {
        return byteArrayOutputStream.toByteArray();
    }

    @Override
    public GameOutput reset() {
        byteArrayOutputStream.reset();
        return this;
    }

    /**
     * 写入list数据 　只能存 int long utf  boolean
     *
     * @param list
     * @return
     * @throws IOException
     */
    public GameOutput writeList(List<?> list) throws IOException {
        int len = list.size();
        this.writeInt(len);
        Object obj;
        for (int i = 0; i < len; i++) {
            obj = list.get(i);
            if (obj instanceof Long) {
                this.writeInt(1);
                this.writeLong((Long) obj);
            } else if (obj instanceof Integer) {
                this.writeInt(2);
                this.writeInt((Integer) obj);
            } else if (obj instanceof Boolean) {
                this.writeInt(3);
                this.writeBoolean((Boolean) obj);
            } else {
                this.writeInt(0);
                this.writeUTF((String) obj);
            }
        }
        return this;
    }

    public GameOutput writeUTF(String s) throws IOException {
        if (s == null) {
            s = "";
        }
        outputStream.writeUTF(s);
        return this;
    }

    public GameOutput writeInt(int v) throws IOException {
        outputStream.writeInt(v);
        return this;
    }

    public GameOutput writeInt(Integer v) throws IOException {
        if (v == null) { v = 0;}
        outputStream.writeInt(v);
        return this;
    }

    public GameOutput writeShort(int v) throws IOException {
        outputStream.writeShort(v);
        return this;
    }

    public GameOutput writeShort(Integer v) throws IOException {
        if (v == null) { v = 0;}
        outputStream.writeShort(v);
        return this;
    }

    public GameOutput writeFloat(float v) throws IOException {
        outputStream.writeFloat(v);
        return this;
    }
    public GameOutput writeFloat(Float v) throws IOException {
        if (v == null) { v = 0f;}
        outputStream.writeFloat(v);
        return this;
    }

    public GameOutput writeDouble(double v) throws IOException {
        outputStream.writeDouble(v);
        return this;
    }
    public GameOutput writeDouble(Double v) throws IOException {
        if (v == null) { v = 0d;}
        outputStream.writeDouble(v);
        return this;
    }

    public GameOutput writeLong(long v) throws IOException {
        outputStream.writeLong(v);
        return this;
    }

    public GameOutput writeLong(Long v) throws IOException {
        if (v == null) { v = 0L;}
        outputStream.writeLong(v);
        return this;
    }

    public GameOutput writeByte(int v) throws IOException {
        outputStream.writeByte(v);
        return this;
    }

    public GameOutput writeBoolean(boolean v) throws IOException {
        outputStream.writeBoolean(v);
        return this;
    }

    public GameOutput writeBoolean(Boolean v) throws IOException {
        if (v == null) { v = false;}
        outputStream.writeBoolean(v);
        return this;
    }

    public GameOutput write(byte[] bytes, int offset, int length) throws IOException {
        outputStream.write(bytes, offset, length);
        return this;
    }

    /**
     * 写入实体
     * @param write
     * @return
     * @throws IOException
     */
    public GameOutput writeEntity(IWrite write) throws IOException {
        write.write(this);
        return this;
    }

    public void close() throws IOException {
        if (this.outputStream != null) this.outputStream.close();
        this.outputStream = null;
    }

    /**
     * 自动判断类型写入
     *
     * @param arge 自动判断类型包括(Boolean、Integer、Long、byte[]、其余全是String)
     * @return
     * @throws IOException
     */
    public GameOutput write(Object... arge) throws IOException {
        Object obj;
        byte[] b;
        for (int i = 0; i < arge.length; i++) {
            obj = arge[i];
            if (obj instanceof Boolean) {
                writeBoolean((Boolean) obj);
            } else if (obj instanceof Integer) {
                writeInt((Integer) obj);
            } else if (obj instanceof Long) {
                writeLong((Long) obj);
            } else if (obj instanceof byte[]) {
                b = (byte[]) obj;
                write(b, 0, b.length);
            } else if (obj instanceof List<?>) {
                writeList((List<?>) obj);
            } else {
                writeUTF(obj == null ? "" : obj.toString());
            }
        }
        return this;
    }

    public static GameOutput get() throws Exception {
        return PoolUtils.getObject(GameOutput.class);
    }
    public static GameOutput get(long borrowMaxWaitMillis) throws Exception {
        return PoolUtils.getObject(GameOutput.class, borrowMaxWaitMillis);
    }
    public static GameOutput get(Duration borrowMaxWaitDuration) throws Exception {
        return PoolUtils.getObject(GameOutput.class, borrowMaxWaitDuration);
    }

    /** 生成数据并回收 */
    public byte[] toByteReturn() {
        byte[] b = this.toByteArray();
        PoolUtils.returnObject(this);
        return b;
    }










}

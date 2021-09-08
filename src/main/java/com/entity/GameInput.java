package com.entity;

import lombok.Getter;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

@Getter
public class GameInput {

    private DataInputStream inputStream;
    private ByteArrayInputStream byteArrayInputStream;

    public GameInput(byte[] buf) {
        byteArrayInputStream = new ByteArrayInputStream(buf);
        inputStream = new DataInputStream(byteArrayInputStream);
    }

    public GameInput(ByteArrayInputStream inputStream) {
        byteArrayInputStream = inputStream;
        this.inputStream = new DataInputStream(inputStream);
    }

    public void reset() {
        byteArrayInputStream.reset();
    }

    public void close() throws IOException {
        if(inputStream!=null) this.inputStream.close();
        inputStream = null;
    }

    public int available() throws IOException {
        return inputStream.available();
    }

    public int readInt() throws IOException {
        return inputStream.readInt();
    }

    public String readUTF() throws IOException {
        return inputStream.readUTF();
    }

    public boolean readBoolean() throws IOException {
        return inputStream.readBoolean();
    }

    public long readLong() throws IOException {
        return inputStream.readLong();
    }

    public float readFloat() throws IOException {
        return inputStream.readFloat();
    }

    public double readDouble() throws IOException {
        return inputStream.readDouble();
    }

    public int read(byte b[]) throws IOException {
        return inputStream.read(b);
    }

    public int read(byte b[], int off, int len) throws IOException {
        return inputStream.read(b, off, len);
    }

}

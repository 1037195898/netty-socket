package com.interfaces;

import com.entity.GameOutput;

import java.io.IOException;

public interface IWrite {

    /**
     * 写入数据
     * @param output 写入流对象
     */
    void write(GameOutput output) throws IOException;

}

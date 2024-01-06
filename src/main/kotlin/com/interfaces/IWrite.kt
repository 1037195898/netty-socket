package com.interfaces

import com.entity.Output

interface IWrite {
    /**
     * 写入数据
     * @param output 写入流对象
     */
    fun write(output: Output?)
}

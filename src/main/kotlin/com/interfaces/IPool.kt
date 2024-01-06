package com.interfaces

interface IPool<T> {
    /** 重置数据  */
    fun reset(): T
}

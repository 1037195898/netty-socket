package com.util

import com.annotation.SocketAction
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.isAccessible

/**
 * 执行事件
 */
object ActionUtils {

    private val actionMap = ConcurrentHashMap<Int, MutableList<ActionMethod>>()


    @JvmStatic
    fun <T : Any> addAction(obj: T) {
        obj.javaClass.kotlin.functions.forEach { funs ->
            funs.findAnnotation<SocketAction>()?.let {
                addAction(it, funs, obj)
            }
        }
    }

    fun <T : Any> addAction(socketAction: SocketAction, method: KFunction<*>, obj: T) {
        addAction(socketAction.value, method, obj)
    }

    fun <T : Any> addAction(value: Int, method: KFunction<*>, obj: T) {
        actionMap.getOrPut(value) { mutableListOf() }.add(ActionMethod(value, method, obj))
    }

    /**
     * 执行方法
     * @param value 事件id
     * @param args 参数
     * @return 有注册方法被调用 返回true
     */
    fun <T : Any> run(value: Int, vararg args: T): Boolean {
        var result = false
        actionMap[value]?.forEach {
            if (runMethod(it, *args)) {
                result = true
            }
        }
        return result
    }

    fun <T : Any> runMethod(actionMethod: ActionMethod, vararg args: T): Boolean {
        val len = args.size
        actionMethod.method.typeParameters
        actionMethod.method.valueParameters
        val parameterCount = actionMethod.method.valueParameters.size
        if (parameterCount <= len) {
            if (parameterCount > 0) {
                val args2 = isVerify(actionMethod.method.valueParameters, *args) ?: return false
                actionMethod.run(args2.toTypedArray())
                return true
            }
            actionMethod.run()
            return true
        }
        return false
    }

    private fun isVerify(cls: List<KParameter>, vararg args: Any): List<*>? {
        val parameterTypes = cls.map { it.type }
        val argsList = args.toMutableList()
        val map = parameterTypes.map { type ->
            val value = argsList.find {
                it::class.starProjectedType.classifier == type.classifier
            }?.apply {
                argsList.remove(this)
            }
            value
        }

        return if (map.any { it == null }) {
            return null
        } else map
    }

    fun removeAction(value: Int) {
        actionMap.remove(value)
    }

    fun removeAction(value: Int, method: ActionMethod) {
        actionMap[value]?.remove(method)
    }
}


data class ActionMethod(
    var value: Int = 0,
    var method: KFunction<*>,
    var objIn: Any
) {
    fun run(args: Array<*>? = null) {
        val obj = args?.let { arrayOf(objIn, *it) } ?: arrayOf(objIn)
        method.isAccessible = true
        method.call(*obj)
        method.isAccessible = false
    }
}

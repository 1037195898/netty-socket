package com.util;

import com.annotation.SocketAction;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.reflect.MethodUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 执行事件
 */
public class ActionUtils {

    private static final Map<Integer, List<ActionMethod>> actionMap = new ConcurrentHashMap<>();


    public static void addAction(Object object) {
        Method[] methods = MethodUtils.getMethodsWithAnnotation(object.getClass(), SocketAction.class);
        for (Method method : methods) {
            SocketAction action = MethodUtils.getAnnotation(method, SocketAction.class, true, true);
            if (action != null) addAction(action, method, object);
        }
    }

    public static void addAction(SocketAction socketAction, Method method, Object object) {
        addAction(socketAction.value(), method, object);
    }

    public static void addAction(int value, Method method, Object object) {
        List<ActionMethod> methods;
        if (actionMap.containsKey(value)) {
            methods = actionMap.get(value);
        } else {
            methods = new ArrayList<>();
            actionMap.put(value, methods);
        }
        methods.add(new ActionMethod().setValue(value).setMethod(method).setObject(object));
    }


    public static void run(int value, Object... args) throws InvocationTargetException, IllegalAccessException {
        List<ActionMethod> methods = actionMap.get(value);
        if (methods != null) {
            for (ActionMethod method : methods) {
                runMethod(method, args);
            }
        }
    }

    private static void runMethod(ActionMethod method, Object... args) throws InvocationTargetException, IllegalAccessException {
        int len = args.length;
        int parameterCount = method.getMethod().getParameterCount();
        if (parameterCount == 0 || (parameterCount <= len && isVerify(method.getMethod().getParameterTypes(), args))) {
            method.run(args);
        }
    }

    private static boolean isVerify(Class<?>[] cls, Object... args) {
        boolean verify = false;
        for (Class<?> c : cls) {
            verify = false;
            for (Object arg : args) {
                if (Objects.equals(c, arg.getClass())) {
                    verify = true;
                    break;
                }
            }
            if (!verify) break;
        }
        return verify;
    }

    public static void removeAction(int value) {
        actionMap.remove(value);
    }

    public static void removeAction(int value, Method method) {
        List<ActionMethod> methods = actionMap.get(value);
        if (methods != null && method != null) {
            methods.remove(method);
        }
    }

}

@Accessors(chain = true)
@Data
class ActionMethod {

    int value;
    Method method;
    Object object;

    public void run(Object[] args) throws InvocationTargetException, IllegalAccessException {
        Class<?>[] types = method.getParameterTypes();
        Object[] obj = new Object[method.getParameterCount()];
        for (int i = 0; i < types.length; i++) {
            for (Object arg : args) {
                if (Objects.equals(types[i], arg.getClass())) {
                    obj[i] = arg;
                }
            }
        }
        method.invoke(object, obj);
    }

}

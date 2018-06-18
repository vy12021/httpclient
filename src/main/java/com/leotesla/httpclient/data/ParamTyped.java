package com.leotesla.httpclient.data;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 参数化类（泛型）
 * 使用限制为必须作用在参数化直接类上才能获取参数
 *
 * @version 1.0
 *
 * Created by LeoTesla on 2017/10/7.
 */

public abstract class ParamTyped {

    /**
     * 获取某个泛型参数
     * @param index     泛型参数位置
     * @return  使用时需要自行转换为类似Class<T>使用
     */
    protected final Class<?> getParamTypeAt(int index) {
        Type type = getClass().getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            Type[] params = ((ParameterizedType) type).getActualTypeArguments();
            if (index < params.length && params[index] instanceof Class) {
                return (Class<?>) params[index];
            }
        }

        return Void.TYPE;
    }

}

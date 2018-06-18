package com.leotesla.httpclient;

import android.support.annotation.NonNull;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;

import java.io.Serializable;
import java.util.List;

/**
 * 基本数据集解析器
 *
 * @version 1.0
 *
 * Created by LeoTesla on 2017/10/5.
 */

public class Parsable<T extends Serializable> {

    private final Class<T> clazz;

    Parsable(Class<T> clazz) {
        this.clazz = clazz;
    }

    /**
     * 解析单对象模型
     * @param json  数据json
     */
    @SuppressWarnings("unchecked")
    public final T parse(@NonNull String json) throws JSONException, NumberFormatException {
        if (String.class == this.clazz) {
            return (T) json;
        } else if (Long.class == this.clazz || long.class == this.clazz) {
            return (T) (Long) Long.parseLong(json);
        } else if (Integer.class == this.clazz || int.class == this.clazz) {
            return (T) (Integer) Integer.parseInt(json);
        } else if (Double.class == this.clazz || double.class == this.clazz) {
            return (T) (Double) Double.parseDouble(json);
        }
        return JSON.parseObject(json, this.clazz);
    }

    /**
     * 解析集合数据模型
     * @param json  数据json
     */
    public final List<T> parseList(@NonNull String json) throws JSONException {
        return JSON.parseArray(json, this.clazz);
    }

}

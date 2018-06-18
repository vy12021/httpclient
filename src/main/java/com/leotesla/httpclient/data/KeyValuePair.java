package com.leotesla.httpclient.data;

import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 键值对
 *
 * @version 1.0
 *
 * Created by LeoTesla on 1/19/2017.
 */

public class KeyValuePair<Key, Value> implements Serializable {

    private static final long serialVersionUID = 6568687317562779553L;

    public final Key key;
    public final Value value;

    public KeyValuePair(Key key, Value value) {
        this.key = key;
        this.value = value;
    }

    /**
     * 转换
     */
    @SafeVarargs
    public static <Key, Value> Map<Key, Value>
    convert2Map(KeyValuePair<Key, Value>... keyValuePairs) {
        ArrayMap<Key, Value> map = new ArrayMap<>();
        for (KeyValuePair<Key, Value> pair : keyValuePairs) {
            map.put(pair.key, pair.value);
        }

        return map;
    }

    /**
     * 合并两个等长list
     */
    public static <Key, Value> List<KeyValuePair<Key, Value>>
    wrapper2List(@NonNull List<Key> keys, @NonNull List<Value> values) {
        if (keys.size() != values.size()) {
            throw new IllegalArgumentException("keys' size not equals values' size");
        }
        List<KeyValuePair<Key, Value>> ret = new ArrayList<>();
        for (int index = 0; index < keys.size(); index++) {
            ret.add(new KeyValuePair<>(keys.get(index), values.get(index)));
        }

        return ret;
    }

    /**
     * 包装单个list
     */
    public static <Value> List<KeyValuePair<String, Value>>
    wrapperSingleList(@NonNull List<Value> values) {
        List<KeyValuePair<String, Value>> ret = new ArrayList<>();
        for (int index = 0; index < values.size(); index++) {
            ret.add(new KeyValuePair<>(values.get(index).toString(), values.get(index)));
        }

        return ret;
    }

    /**
     * 提取value到集合中
     */
    public static <Key, Value> List<Value> getValues(
            @NonNull Collection<KeyValuePair<Key, Value>> collection) {
        List<Value> result = new ArrayList<>(collection.size());
        for (KeyValuePair<Key, Value> pair : collection) {
            result.add(pair.value);
        }
        return result;
    }

    /**
     * 提取key到集合中
     */
    public static <Key, Value> List<Key>
    getKeys( @NonNull Collection<KeyValuePair<Key, Value>> collection) {
        List<Key> result = new ArrayList<>(collection.size());
        for (KeyValuePair<Key, Value> pair : collection) {
            result.add(pair.key);
        }
        return result;
    }

    /**
     * 拆分key和value为分离的List集合
     */
    public static <Key, Value> KeyValuePair<List<Key>, List<Value>>
    slip(KeyValuePair<Key, Value>... pairs) {
        List<Key> keys = new ArrayList<>(pairs.length);
        List<Value> values = new ArrayList<>(pairs.length);
        for (KeyValuePair<Key, Value> pair : pairs) {
            keys.add(pair.key);
            values.add(pair.value);
        }
        return new KeyValuePair<>(keys, values);
    }

    @Override
    public String toString() {
        return "KeyValuePair{" +
                "key=" + key +
                ", value=" + value +
                '}';
    }
}

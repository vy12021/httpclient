package com.leotesla.httpclient.data;

import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;

import java.util.Map;


/**
 * 多状态切换
 *
 * @version 1.0
 *
 * Created by Tesla on 2016/4/1.
 */
public class StateSwitcher<K, V> {

    private ArrayMap<K, V> states;
    private int index = -1;

    private StateSwitcher(Map<K, V> states) {
        this.states = new ArrayMap<>(states.size());
        this.states.putAll(states);
    }

    public synchronized KeyValuePair<K, V> next() {
        if (null != states && !states.isEmpty()) {
            if (index == states.size() - 1) {
                index = 0;
            } else {
                index++;
            }

            return new KeyValuePair<>(states.keyAt(index), states.valueAt(index));
        }

        return null;
    }

    public synchronized KeyValuePair<K, V> get() {
        if (null != states && !states.isEmpty()) {
            if (-1 == index) next();
            return new KeyValuePair<>(states.keyAt(index), states.valueAt(index));
        }

        return null;
    }

    public synchronized KeyValuePair<K, V> previous() {
        if (null != states && !states.isEmpty()) {
            if (index == 0 || -1 == index) {
                index = states.size() - 1;
            } else {
                index--;
            }

            return new KeyValuePair<>(states.keyAt(index), states.valueAt(index));
        }

        return null;
    }

    public synchronized StateSwitcher<K, V> clear() {
        this.states.clear();
        this.index = -1;

        return this;
    }

    @SafeVarargs
    public final synchronized StateSwitcher<K, V> add(@NonNull KeyValuePair<K, V>... states) {
        this.states.putAll(KeyValuePair.convert2Map(states));
        this.index = -1;

        return this;
    }

    public static final class Builder<K, V> {

        public final StateSwitcher<K, V> build(@NonNull ArrayMap<K, V> states) {
            return new StateSwitcher<>(states);
        }

        @SafeVarargs
        public final StateSwitcher<K, V> build(@NonNull KeyValuePair<K, V>... states) {
            return new StateSwitcher<>(KeyValuePair.convert2Map(states));
        }

    }

}

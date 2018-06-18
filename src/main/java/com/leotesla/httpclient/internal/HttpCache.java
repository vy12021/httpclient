package com.leotesla.httpclient.internal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.leotesla.httpclient.secret.EncryptKits;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Http 缓存处理
 *
 * @version 1.0
 *
 * @see DiskLruCache
 * Created by Leo on 10/24/2017.
 */

final class HttpCache {

    private final static boolean ENCRYPT = true;

    // 请求时间戳
    private final static int POS_TSP = 0;
    // 响应内容
    private final static int POS_CONTENT = 1;
    // 响应头部
    private final static int POS_HEAD = 2;

    private final static Map<String, DiskLruCache> CACHES = new HashMap<>(2);

    private DiskLruCache diskCache;

    private String rc4Key;

    private HttpCache(@NonNull HttpConfig config) {
        try {
            this.rc4Key = "BHB" + new File(config.getCacheDir()).getName() + config.getCacheVersion();
            this.diskCache = CACHES.get(this.rc4Key);
            if (null == this.diskCache || this.diskCache.isClosed()) {
                this.diskCache = DiskLruCache.open(
                        new File(config.getCacheDir()), config.getCacheVersion(),
                        3, config.getCacheSize());
                CACHES.put(this.rc4Key, this.diskCache);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 打开缓存会话
     * @param config    配置
     */
    synchronized static HttpCache open(@NonNull HttpConfig config) throws IOException {
        return new HttpCache(config);
    }

    /**
     * 预加载部分缓存数据
     * @param request       请求
     */
    synchronized void preload(@NonNull HttpRequest request) {
        // 注入请求头
        try {
            request.getResponse().headers = readHead(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 预读取缓存头部
     * @param request   请求
     */
    private synchronized Map<String, List<String>>
    readHead(@NonNull HttpRequest request) throws IOException {
        Map<String, List<String>> heads = null;
        if (null != this.diskCache && HttpMethod.GET == request.getMethod()) {
            DiskLruCache.Snapshot snapshot = this.diskCache.get(generateKey(request));
            heads = parseHead(snapshot);
        }
        return heads;
    }

    /**
     * 解析头部缓存信息
     * @param snapshot  缓存快照
     */
    private Map<String, List<String>> parseHead(DiskLruCache.Snapshot snapshot) throws IOException {
        Map<String, List<String>> heads = null;
        if (null != snapshot) {
            String head = snapshot.getString(POS_HEAD);
            try {
                JSONObject object = JSON.parseObject(head);
                if (null != object && !object.isEmpty()) {
                    heads = new ArrayMap<>(object.size());
                    for (String key : object.keySet()) {
                        JSONArray array = object.getJSONArray(key);
                        if (null != array) {
                            List<String> value = new ArrayList<>(array.size());
                            for (int i = 0; i < array.size(); i++) {
                                value.add(array.getString(i));
                            }
                            heads.put(key, value);
                        }
                    }
                }
            } catch (Exception ignore) {
                ignore.printStackTrace();
            }
        }
        return heads;
    }

    /**
     * 尝试读取缓存
     * @param request   请求体
     * @return          缓存响应体
     */
    @Nullable
    synchronized HttpResponse read(@NonNull HttpRequest request) throws IOException {
        HttpResponse response = null;
        if (null != this.diskCache && HttpMethod.GET == request.getMethod()) {
            String key = generateKey(request);
            DiskLruCache.Snapshot snapshot = this.diskCache.get(key);
            CacheConfig config = request.getConfig().getCacheConfig();
            if (null != snapshot) {
                long lastTsp = Long.parseLong(snapshot.getString(POS_TSP));
                Map<String, List<String>> parseHead = parseHead(snapshot);
                int age = (int) (System.currentTimeMillis() - lastTsp);
                if (CacheStrategy.Disable == config.strategy) {
                    return null;
                } else if (config.strategy == CacheStrategy.Must ||
                        config.expiredMs > age || (request.isExecuted() && config.loadStale)) {
                    String content = ENCRYPT ? EncryptKits.decryptRC4(
                            snapshot.getString(POS_CONTENT), this.rc4Key)
                            : snapshot.getString(POS_CONTENT);
                    if (!TextUtils.isEmpty(content)) {
                        response = HttpResponse.fromCache(request, content, parseHead);
                    } else {
                        this.diskCache.remove(key);
                    }
                }
            }
        }
        return response;
    }

    /**
     * 写缓存
     * @param response  响应
     */
    synchronized HttpCache write(@NonNull HttpResponse response) throws IOException {
        if (null != this.diskCache && !response.isCache
                && null == response.exception
                && HttpMethod.GET == response.request.getMethod()
                && !TextUtils.isEmpty(response.getContent())) {
            DiskLruCache.Editor editor = this.diskCache.edit(generateKey(response.request));
            editor.set(POS_TSP, String.valueOf(System.currentTimeMillis()));
            editor.set(POS_CONTENT, ENCRYPT ? EncryptKits.encryptRC4(
                    response.getContent(), this.rc4Key) : response.getContent());
            editor.set(POS_HEAD, HttpHelper.toJSONString(response.getHeader(true)));
            editor.commit();
        }
        return this;
    }

    /**
     * 当前缓存文件大小 in bytes
     */
    long size() {
        if (null != this.diskCache) {
            return this.diskCache.size();
        }
        return -1;
    }

    /**
     * 缓存最大限制 in bytes
     */
    long maxSize() {
        if (null != this.diskCache) {
            return this.diskCache.maxSize();
        }
        return -1;
    }

    /**
     * 删除所有缓存
     */
    void delete() {
        try {
            if (null != this.diskCache) {
                this.diskCache.delete();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 刷新数据到磁盘
     */
    synchronized void flush() {
        try {
            if (null != this.diskCache && !this.diskCache.isClosed()) {
                this.diskCache.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭缓存会话
     */
    synchronized void close() {
        try {
            flush();
        } finally {
            try {
                if (null != this.diskCache) {
                    this.diskCache.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String generateKey(@NonNull HttpRequest request) {
        return EncryptKits.MD5(request.getBody().toString(), false);
    }

}

package com.leotesla.httpclient.secret;

/**
 * Hash 散列加密类型
 *
 * @version 1.0
 *
 * Created by LeoTesla on 8/1/2016.
 */
public enum HashType {

    MD5("MD5"),
    SHA("SHA"),
    SHA1("SHA-1");

    String type;

    String getType() {
        return type;
    }

    HashType(String type) {
        this.type = type;
    }

}

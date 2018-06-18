package com.leotesla.httpclient.secret;

import android.support.annotation.NonNull;

import java.nio.charset.Charset;

/**
 * RC4 快速加密算法
 */
public class RC4 {

    /**
     * 解密
     * @param data  密文字节
     * @param key   密钥
     */
    public static String decryptRC4(byte[] data, String key) {
        if (data == null || key == null) {
            return null;
        }
        return asString(RC4compute(data, key));
    }

    /**
     * 解密
     * @param data  密文字符串
     * @param key   密钥
     */
    public static String decryptRC4(String data, String key) {
        if (data == null || key == null) {
            return null;
        }
        return new String(RC4compute(HexString2Bytes(data), key));
    }

    /**
     * 加密为字节
     * @param data  明文
     * @param key   密钥
     */
    public static byte[] encryptRC4ToBytes(String data, String key) {
        if (data == null || key == null) {
            return null;
        }
        byte b_data[] = data.getBytes();
        return RC4compute(b_data, key);
    }

    /**
     * 加密为字符串
     * @param data  明文
     * @param key   密钥
     */
    public static String encryptRC4ToString(String data, String key) {
        if (data == null || key == null) {
            return null;
        }
        return toHexString(asString(encryptRC4ToBytes(data, key)));
    }

    private static String asString(byte[] buffer) {
        StringBuilder sb = new StringBuilder(buffer.length);
        for (byte b : buffer) {
            sb.append((char) b);
        }
        return sb.toString();
    }

    private static byte[] initKey(@NonNull String key) {
        byte[] b_key = key.getBytes();
        byte state[] = new byte[256];

        for (int i = 0; i < 256; i++) {
            state[i] = (byte) i;
        }
        int index1 = 0;
        int index2 = 0;
        for (int i = 0; i < 256; i++) {
            index2 = ((b_key[index1] & 0xff) + (state[i] & 0xff) + index2) & 0xff;
            byte tmp = state[i];
            state[i] = state[index2];
            state[index2] = tmp;
            index1 = (index1 + 1) % b_key.length;
        }
        return state;
    }

    private static String toHexString(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            int ch = (int) s.charAt(i);
            String s4 = Integer.toHexString(ch & 0xFF);
            if (s4.length() == 1) {
                s4 = '0' + s4;
            }
            sb.append(s4);
        }
        return sb.toString();
    }

    private static byte[] HexString2Bytes(String src) {
        int size = src.length();
        byte[] ret = new byte[size / 2];
        byte[] tmp = src.getBytes(Charset.defaultCharset());
        for (int i = 0; i < size / 2; i++) {
            ret[i] = uniteBytes(tmp[i * 2], tmp[i * 2 + 1]);
        }
        return ret;
    }

    private static byte uniteBytes(byte src0, byte src1) {
        char _b0 = (char) Byte.decode("0x" + new String(new byte[]{src0})).byteValue();
        _b0 = (char) (_b0 << 4);
        char _b1 = (char) Byte.decode("0x" + new String(new byte[]{src1})).byteValue();
        return (byte) (_b0 ^ _b1);
    }

    private static byte[] RC4compute(byte[] input, String mKkey) {
        int x = 0;
        int y = 0;
        byte key[] = initKey(mKkey);
        int xorIndex;
        byte[] result = new byte[input.length];

        for (int i = 0; i < input.length; i++) {
            x = (x + 1) & 0xff;
            y = ((key[x] & 0xff) + y) & 0xff;
            byte tmp = key[x];
            key[x] = key[y];
            key[y] = tmp;
            xorIndex = ((key[x] & 0xff) + (key[y] & 0xff)) & 0xff;
            result[i] = (byte) (input[i] ^ key[xorIndex]);
        }
        return result;
    }

}
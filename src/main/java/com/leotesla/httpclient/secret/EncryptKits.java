package com.leotesla.httpclient.secret;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 加密，解密
 *
 * @version 1.0
 *
 * Created by Tesla on 2015/1/17.
 */
public final class EncryptKits {

    /**
     * 将二进制转换成16进制字符串表示
     * @param input             输入字节数组
     * @param isUpperCase       是否大写
     * @return
     */
    public static String parseByte2HexStr(byte[] input, Boolean isUpperCase) {

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < input.length; i++) {
            String hex = Integer.toHexString(input[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(isUpperCase ? hex.toUpperCase() : hex);
        }

        return sb.toString();
    }

    /**将16进制字符串转换为二进制数组
     * @param input
     * @return
     */
    public static byte[] parseHexStr2Byte(String input) {

        byte[] output = new byte[input.length() / 2];

        if (input.length() < 1) {
            return null;
        }
        for (int i = 0; i < input.length() / 2; i++) {
            int high = Integer.parseInt(input.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(input.substring(i * 2 + 1, i * 2 + 2), 16);
            output[i] = (byte) (high * 16 + low);
        }

        return output;
    }

    /**
     * 获取文件的Hash校验值
     * @param input         文件路径
     * @param hashType      Hash类型
     * @param isUpperCase   结果是否大写
     * @return
     */
    public static String obtainFileHash(String input, HashType hashType, Boolean isUpperCase) {
        String output = "";

        File file = new File(input);
        if (file.isFile()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                output = obtainFileHash(fis, hashType, isUpperCase);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (null != fis) fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return output;
    }

    /**
     * 获取文件的Hash校验值
     * @param fis           文件路径
     * @param hashType      Hash类型
     * @param isUpperCase   结果是否大写
     * @return
     */
    public static String obtainFileHash(@NonNull FileInputStream fis, HashType hashType, Boolean isUpperCase) {
        String output = "";

        try {
            MessageDigest digest = MessageDigest.getInstance(hashType.getType());
            byte[] buffer = new byte[1024];
            int len;
            while (-1 != (len = fis.read(buffer))) {
                digest.update(buffer, 0, len);
            }
            output = parseByte2HexStr(digest.digest(), isUpperCase);
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return output;
    }

    /**
     * 散列SHA1加密
     * @param input
     * @param isUpperCase
     * @return
     */
    public static String SHA1(String input, Boolean isUpperCase) {
        String output = "";

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.update(input.getBytes());
            byte messageDigest[] = digest.digest();
            output = parseByte2HexStr(messageDigest, isUpperCase);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return output;
    }

    /**
     * 散列SHA加密
     * @param input         输入字符串
     * @param isUpperCase   是否大写
     * @return
     */
    public static String SHA(String input, Boolean isUpperCase) {
        String output = "";

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA");
            digest.update(input.getBytes());
            byte messageDigest[] = digest.digest();
            output = parseByte2HexStr(messageDigest, isUpperCase);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return output;
    }

    /**
     * 散列MD5加密
     * @param input
     * @param isUpperCase 是否大写
     * @return
     */
    public static String MD5(String input, Boolean isUpperCase) {
        return MD5(input.getBytes(), isUpperCase);
    }

    /**
     * 散列MD5加密
     * @param input
     * @param isUpperCase 是否大写
     * @return
     */
    public static String MD5(byte[] input, Boolean isUpperCase) {

        String output = "";

        try {
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            mdInst.update(input);
            // 获得密文
            byte[] md = mdInst.digest();
            output = parseByte2HexStr(md, isUpperCase);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return output;
    }

    /**
     * 对称AES加密
     * @param input     需要加密的内容
     * @param password  加密密码 (最小32位，必须为8的倍数)
     * @return
     */
    public static byte[] encryptAES(String input, String password) {
        return encryptAES(input.getBytes(), password);
    }

    /**
     * 对称AES加密
     * @param input     需要加密的内容
     * @param password  加密密码 (最小32位，必须为8的倍数)
     * @return
     */
    public static byte[] encryptAES(byte[] input, String password) {

        byte[] output = null;
        try {
            byte[] key = MD5(password, false).substring(0, 16).getBytes();
            SecretKeySpec sks = new SecretKeySpec(key, "AES");
            // 创建密码器
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            // 初始化
            cipher.init(Cipher.ENCRYPT_MODE, sks, new IvParameterSpec(key));
            output = cipher.doFinal(input);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return output;
    }


    /**
     * 对称AES解密
     * @param input     待解密内容
     * @param password  解密密钥  (最小32位，必须为8的倍数)
     * @return
     */
    public static byte[] decryptAES(byte[] input, String password) {

        byte[] output = null;

        try {
            byte[] key = MD5(password, false).substring(0, 16).getBytes();
            SecretKeySpec sks = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, sks, new IvParameterSpec(key));
            output = cipher.doFinal(input);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output;
    }

    /**
     * 创建RSA密钥对生成器，指定加密和解密算法为RSA
     * @param keySize 输入密钥长度
     * @return
     */
    public static String[] keyGenerateRSA(int keySize){
        // 用来存储密钥的e n d p q
        String[] output = new String[5];
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            // 指定密钥的长度，初始化密钥对生成器
            kpg.initialize(keySize);
            // 生成密钥对
            KeyPair kp = kpg.generateKeyPair();
            RSAPublicKey puk = (RSAPublicKey) kp.getPublic();
            RSAPrivateCrtKey prk = (RSAPrivateCrtKey) kp.getPrivate();
            BigInteger e = puk.getPublicExponent();
            BigInteger n = puk.getModulus();
            BigInteger d = prk.getPrivateExponent();
            BigInteger p = prk.getPrimeP();
            BigInteger q = prk.getPrimeQ();
            output[0] = e.toString();
            output[1] = n.toString();
            output[2] = d.toString();
            output[3] = p.toString();
            output[4] = q.toString();
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        }

        return output;
    }


    /**
     * 加密 在RSA公钥中包含有两个整数信息：e和n。对于明文数字m,计算密文的公式是m的e次方再与n求模。
     * @param input         原数据
     * @param pubKey        公钥
     * @param nStr          n 值
     */
    public static String encryptRSA(String input, String pubKey, String nStr){
        return encryptRSA(input.getBytes(), pubKey, nStr);
    }

    /**
     * 加密 在RSA公钥中包含有两个整数信息：e和n。对于明文数字m,计算密文的公式是m的e次方再与n求模。
     * @param input         原数据
     * @param pubKey        公钥
     * @param nStr          n 值
     */
    public static String encryptRSA(byte[] input, String pubKey, String nStr){

        String output = "";

        try {
            BigInteger e = new BigInteger(pubKey);
            BigInteger n = new BigInteger(nStr);
            // 获取明文的大整数
            BigInteger m = new BigInteger(input);
            BigInteger c = m.modPow(e, n);
            output = c.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return output;
    }

    /**
     * RSA解密
     * @param input     输入
     * @param priKey    私钥
     * @param nStr      参数n
     */
    public static byte[] decryptRSA(String input, String priKey, String nStr){

        byte[] output;

        BigInteger d = new BigInteger(priKey);
        BigInteger n = new BigInteger(nStr);
        BigInteger c = new BigInteger(input);
        // 解密明文
        BigInteger m = c.modPow(d, n);
        // 计算明文对应的字符串并输出
        output = m.toByteArray();

        return output;
    }

    /**
     * RC4 加密
     * @param input 明文
     * @param key   密钥
     */
    public static String encryptRC4(String input, String key) {
        return RC4.encryptRC4ToString(input, key);
    }

    /**
     * RC4 解密
     * @param input 密文
     * @param key   密钥
     */
    public static String decryptRC4(String input, String key) {
        return RC4.decryptRC4(input, key);
    }

    /**
     * 加解密文件示例
     *
     * @param sign      加密签名
     * @param input     输入文件
     * @param en        true: 加密； false:  解密
     * @return          true: 操作成功
     */
    public synchronized static boolean cryptFile(@NonNull char[] sign,
                                                 @NonNull File input,
                                                 boolean en) {

        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(input, "rws");
            raf.seek(0);
            byte[] header = new byte[100];
            int header_len = raf.read(header);
            byte[] tail = null;
            int tail_len = 0;
            int pre_slice_len = 83, pre_slice_start_index = 15;
            int pre_start_index = sign.length, pre_len_index = sign.length + 1,
                    en_tail_len_index = sign.length + 2, key_len_index = sign.length + 3;
            boolean ret = true;
            if (sign.length > 5) {
                throw new IllegalArgumentException("sign length must be less than 5");
            }
            for (int index = 0; index < sign.length; index++) {
                if (header[index] != sign[index]) {
                    ret = false;
                    break;
                }
            }
            if (ret) {
                raf.seek(input.length() - (header[en_tail_len_index] + header[key_len_index]));
                tail = new byte[header[en_tail_len_index] + header[key_len_index]];
                tail_len = raf.read(tail);
                loge("tail.md5: " + EncryptKits.MD5(tail, true));
            }
            raf.seek(0);
            if (header_len == header.length && en) {
                loge("加密文件：" + input.getAbsolutePath());
                String key =  EncryptKits.MD5(header, true);
                byte[] encrypt = EncryptKits.encryptAES(header, key);
                byte[] key_byte = key.getBytes();
                String sliceSec = EncryptKits.parseByte2HexStr(
                        Arrays.copyOfRange(encrypt, pre_slice_len, encrypt.length), true);
                // loge("加密前 length：" + input.length() + "; header.md5: " + key);
                // 100字节经过32位加密结果为112位, sliceSec 长度为固定
                for (int index = 0; index < sign.length; index++) {
                    raf.write(sign[index]);
                }
                raf.write(pre_slice_start_index);
                raf.write(pre_slice_len);
                raf.write(encrypt.length - pre_slice_len);
                raf.write(key_byte.length);
                raf.seek(pre_slice_start_index);
                raf.write(encrypt, 0, pre_slice_len);

                raf.seek(input.length());
                raf.setLength(input.length() + encrypt.length - pre_slice_len + key_byte.length);

                raf.write(encrypt, pre_slice_len, encrypt.length - pre_slice_len);
                raf.write(key_byte);

                byte[] en_tail = Arrays.copyOfRange(encrypt, pre_slice_len, encrypt.length);
                byte[] _tail = Arrays.copyOf(en_tail, encrypt.length - pre_slice_len + key_byte.length);
                System.arraycopy(key_byte, 0, _tail, encrypt.length - pre_slice_len, key_byte.length);

                /*loge("加密后 encrypt.md5: " + EncryptKits.MD5(encrypt, true) +
                        "; _tail.md5: " + EncryptKits.MD5(_tail, true));*/

            } else if (header.length == header_len && null != tail && tail_len == tail.length) {

                loge("还原文件：" + input.getAbsolutePath());

                byte[] en_header = Arrays.copyOfRange(header, header[pre_start_index], header[pre_start_index] + header[pre_len_index]);
                byte[] pre_decrypt = Arrays.copyOf(en_header, header[pre_len_index] + header[en_tail_len_index] + header[key_len_index]);
                System.arraycopy(tail, 0, pre_decrypt, header[pre_len_index], header[en_tail_len_index] + header[key_len_index]);

                byte[] key_slice = Arrays.copyOfRange(pre_decrypt, pre_decrypt.length - header[key_len_index], pre_decrypt.length);
                byte[] decrypt_slice = Arrays.copyOfRange(pre_decrypt, 0, pre_decrypt.length - header[key_len_index]);

                String key = new String(key_slice);
                byte[] decrypt = EncryptKits.decryptAES(decrypt_slice, key);

                raf.setLength(input.length() - (header[en_tail_len_index] + header[key_len_index]));
                raf.seek(0);
                raf.write(decrypt);

                /*loge("解密后 file.length: " + input.length() +
                        "; decrypt.length: " + decrypt.length +
                        "; decrypt.md5: " + EncryptKits.MD5(decrypt, true));*/
            } else {
                loge("file: " + input.getAbsolutePath() + ", 数据异常");
            }
        } catch (Exception e) {
            e.printStackTrace();
            loge((en ? "加密" : "解密") + "失败：" + e.getMessage());
            return false;
        } finally {
            try {
                if (null != raf) raf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    private static void loge(String msg) {
        Log.e("EncryptKits", msg);
    }

}


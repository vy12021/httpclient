package com.leotesla.httpclient.internal;

import android.net.http.SslCertificate;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

import com.leotesla.httpclient.data.KeyValuePair;
import com.leotesla.httpclient.secret.EncryptKits;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * 证书管理
 *
 * @version 1.0
 *
 * Created by Tesla on 2016/7/24.
 */
public final class SSLManager {

    // 默认证书名称
    public static final String DEFAULT_CERT_HOST = "default";

    // 普通请求ssl证书工厂
    private final static Map<String, KeyValuePair<DefaultSSL, SimpleSSL>>
            SSL_SOCKET_FACTORY = new ArrayMap<>();
    // WebView ssl 证书工厂
    private final static Map<String, KeyValuePair<PrivateKey, Certificate[]>>
            WEB_VIEW_SSL = new ArrayMap<>();

    /**
     * 初始化ssl认证管理器，以后就只读取缓存
     * @param certificates  本地校验证书族, key为域名（也为别名）,value为证书流
     */
    static void initSSLSocketFactory(Map<String, InputStream> certificates) {
        if (null == certificates || certificates.isEmpty()) return;
        genSSLSocketFactory(null, certificates);
    }

    /**
     * 是否已经缓存，防止重复获取
     * @param host  域名
     */
    public static boolean isInitialed(String host) {
        return SSL_SOCKET_FACTORY.containsKey(host);
    }

    /**
     * 查询获取缓存证书认证管理器
     * @param host  请求域
     * @return      缓存
     */
    static KeyValuePair<DefaultSSL, SimpleSSL> getSSLFactory(String host, boolean loadDefault) {
        if (TextUtils.isEmpty(host) && !loadDefault) return null;
        for (String key : SSL_SOCKET_FACTORY.keySet()) {
            if (!TextUtils.isEmpty(key) && host.endsWith(key)) {
                return SSL_SOCKET_FACTORY.get(key);
            }
        }
        if (loadDefault) {
            return SSL_SOCKET_FACTORY.get(DEFAULT_CERT_HOST);
        }
        return null;
    }

    /**
     * 获取WebView证书结构
     */
    public static KeyValuePair<PrivateKey, Certificate[]>
    getWebViewSSL(@NonNull String host, @NonNull InputStream is,
                  @NonNull String storePw, @NonNull String alias, @NonNull String certPw) {
        try {
            if (WEB_VIEW_SSL.containsKey(host)) {
                return WEB_VIEW_SSL.get(host);
            }
            KeyStore keystore = KeyStore.getInstance("JKS");
            keystore.load(is, storePw.toCharArray());
            PrivateKey key = (PrivateKey) keystore.getKey(alias, certPw.toCharArray());
            Certificate[] certChain = keystore.getCertificateChain(alias);
            KeyValuePair<PrivateKey, Certificate[]> ret = new KeyValuePair<>(key, certChain);
            WEB_VIEW_SSL.put(host, ret);
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * SSL证书错误，手动校验https证书
     *
     * @param cert      https证书
     * @param sha256Str sha256值
     * @return true通过，false失败
     */
    public static boolean isSSLCertOk(SslCertificate cert, String sha256Str) {
        byte[] SSLSHA256 = EncryptKits.parseHexStr2Byte(sha256Str);
        Bundle bundle = SslCertificate.saveState(cert);
        if (bundle != null) {
            byte[] bytes = bundle.getByteArray("x509-certificate");
            if (bytes != null) {
                try {
                    CertificateFactory cf = CertificateFactory.getInstance("X.509");
                    Certificate ca = cf.generateCertificate(new ByteArrayInputStream(bytes));
                    MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
                    byte[] key = sha256.digest(ca.getEncoded());
                    return Arrays.equals(key, SSLSHA256);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    /**
     * 生产认证管理器
     * @param keyManagers  KeyManager[]，如果为空就为单向认证
     * @param certificates 证书的输入流
     */
    static void genSSLSocketFactory(KeyManager[] keyManagers,
                                    Map<String, InputStream> certificates) {
        try {
            CertificateFactory cerFactory = CertificateFactory.getInstance("X.509");
            for (String host : certificates.keySet()) {
                if (SSL_SOCKET_FACTORY.containsKey(host)) {
                    continue;
                }
                InputStream certStream = certificates.get(host);
                Certificate certificate = cerFactory.generateCertificate(certStream);
                DefaultSSL defaultSSL = genDefaultSSLFactory(keyManagers, host, certificate);
                SimpleSSL simpleSSL = genSimpleSSLFactory(keyManagers, host, certificate);
                if (certStream != null) certStream.close();
                SSL_SOCKET_FACTORY.put(host, new KeyValuePair<>(defaultSSL, simpleSSL));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获得双向认证所需的参数
     *
     * @param bks          bks证书的输入流
     * @param keystorePass 秘钥
     * @return KeyManager[]对象
     */
    static KeyManager[] getKeyManagers(InputStream bks, String keystorePass) {
        KeyStore clientKeyStore;
        try {
            clientKeyStore = KeyStore.getInstance("BKS");
            clientKeyStore.load(bks, keystorePass.toCharArray());
            KeyManagerFactory keyManagerFactory =
                    KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(clientKeyStore, keystorePass.toCharArray());
            return keyManagerFactory.getKeyManagers();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 生产默认证书处理器
     * @param keyManagers   key manager
     * @param alias         证书别名
     * @param certificate   证书描述
     */
    private static DefaultSSL genDefaultSSLFactory(KeyManager[] keyManagers,
                                                   String alias, Certificate certificate)
            throws NoSuchAlgorithmException, KeyStoreException, IOException,
            CertificateException, KeyManagementException {
        TrustManagerFactory trustManagerFactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null);
        keyStore.setCertificateEntry(alias, certificate);
        trustManagerFactory.init(keyStore);
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
            throw new IllegalStateException("Unexpected default trust managers:"
                    + Arrays.toString(trustManagers));
        }
        X509TrustManager trustManager = (X509TrustManager) trustManagers[0];
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagers, new TrustManager[]{trustManager}, null);
        return new DefaultSSL(sslContext.getSocketFactory(), trustManager);
    }

    /**
     * 生产自定义证书处理器
     * @param keyManagers   key manager
     * @param alias         证书别名
     * @param certificate   证书描述
     */
    private static SimpleSSL genSimpleSSLFactory(KeyManager[] keyManagers,
                                                 String alias, Certificate certificate)
            throws NoSuchAlgorithmException, KeyManagementException {
        X509TrustManager simpleTrustManager =
                new SimpleTrustManager(Collections.singletonList(certificate));
        SSLContext sslContext = SSLContext.getInstance("TLS");
        // 第一个参数用于双向验证
        sslContext.init(keyManagers, new TrustManager[]{simpleTrustManager}, null);
        return new SimpleSSL(sslContext.getSocketFactory(), simpleTrustManager);
    }


    /**
     * 简化ssl证书校验方式
     */
    private static class SimpleTrustManager implements X509TrustManager {

        private final List<Certificate> certificates;

        SimpleTrustManager(List<Certificate> certificates) {
            this.certificates = certificates;
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            if (null != chain) {
                verifyCertificatesGroup(certificates, Arrays.asList(chain));
            } else {
                throw new CertificateException("Server certificate not found");
            }
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

        /**
         * 校验证书组，如果发现有一个可以校验成功就返回信任
         * @param clientCerts   客户端证书组
         * @param serverCerts   服务端证书组
         */
        private static void verifyCertificatesGroup(List<Certificate> clientCerts,
                                                    List<Certificate> serverCerts)
                throws CertificateException {
            if (clientCerts == null || clientCerts.isEmpty() || clientCerts.get(0) == null) {
                throw new CertificateException("Client certificate not found");
            }
            if (serverCerts == null || serverCerts.isEmpty() || serverCerts.get(0) == null) {
                throw new CertificateException("Server certificate not found");
            }
            try {
                byte[] spk, cpk;
                for (Certificate server : serverCerts) {
                    spk = server.getPublicKey().getEncoded();
                    for (Certificate client : clientCerts) {
                        cpk = client.getPublicKey().getEncoded();
                        if (Arrays.equals(spk, cpk)) {
                            return;
                        }
                    }
                }
            } catch (Exception ignore) {
                throw new CertificateException("Unknown exception: " + ignore.getLocalizedMessage());
            }
            throw new CertificateException("Certificate not found");
        }

    }

    /**
     * 系统标准ssl校验机制
     */
    final static class DefaultSSL {

        final SSLSocketFactory factory;
        final X509TrustManager trustManager;

        DefaultSSL(SSLSocketFactory factory, X509TrustManager trustManager) {
            this.factory = factory;
            this.trustManager = trustManager;
        }
    }

    /**
     * 自定义ssl校验
     */
    final static class SimpleSSL {

        final SSLSocketFactory factory;
        final X509TrustManager trustManager;

        SimpleSSL(SSLSocketFactory factory, X509TrustManager trustManager) {
            this.factory = factory;
            this.trustManager = trustManager;
        }
    }

}
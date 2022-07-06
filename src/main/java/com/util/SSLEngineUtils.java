package com.util;

import org.apache.commons.lang3.StringUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 例子：<br/>
 * <pre>
 *     URI uri = URI.create("wss://www.test.com/dev");
 *     int port = uri.getPort();
 *     if (port == -1) {
 *        if (uri.getScheme().equals("wss")) {
 *          port = 443;
 *        } else {
 *          port = 80;
 *        }
 *     }
 *     SSLContext content = SSLEngineUtils.getSSLContext();
 *     SSLEngine sslEngine = content.createSSLEngine(uri.getHost(), port);
 *     sslEngine.setUseClientMode(true);
 *     sslEngine.setNeedClientAuth(false);
 *     pipeline.addFirst("ssl", new SslHandler(sslEngine));
 * </pre>
 */
public class SSLEngineUtils {

    /**
     * 证书路径 可以是文件夹或者文件 证书格式 *.cer
     */
    public static String cerPath = "data/cer";
    public static String TLS = "TLSv1.2";

    public static SSLContext getSSLContext() throws Exception {
        KeyStore keyStore = buildKeyStore();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);
        SSLContext sslContext = StringUtils.isBlank(TLS) ? SSLContext.getDefault() : SSLContext.getInstance(TLS);
        sslContext.init(null, tmf.getTrustManagers(), null);
        return sslContext;
    }

    private static KeyStore buildKeyStore() throws Exception {
        // 获取自签名的证书
        List<X509Certificate> certs = loadHttpsCerts();
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init((KeyStore) null);
        // 获得jdk证书库中的证书
        Arrays.stream(trustManagerFactory.getTrustManagers())
                .filter(manager -> manager instanceof X509TrustManager)
                .findFirst()
                .ifPresent(manager -> {
                    List<X509Certificate> jdkCerts = Arrays.asList(((X509TrustManager) manager).getAcceptedIssuers());
                    certs.addAll(jdkCerts);
                });
        certs.forEach(cert -> {
            try {
                keyStore.setCertificateEntry(cert.getSubjectDN().getName(), cert);
            } catch (KeyStoreException e) {
                e.printStackTrace();
            }
        });
        return keyStore;
    }

    private static List<X509Certificate> loadHttpsCerts() throws IOException, CertificateException {
        List<X509Certificate> certs = new ArrayList<>();
        File file = new File(cerPath);
        if (file.isDirectory()) {
            File[] fileList = file.listFiles();
            if (fileList != null) {
//                System.out.println("有cer=" + fileList.length);
                for (File value : fileList) {
                    certs.add(loadCer(value));
                }
            }
        } else {
            certs.add(loadCer(file));
        }
        return certs;
    }

    private static X509Certificate loadCer(File file) throws IOException, CertificateException {
        FileInputStream inputStream = new FileInputStream(file);
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) factory.generateCertificate(inputStream);
    }

}

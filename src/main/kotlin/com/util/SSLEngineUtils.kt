package com.util

import java.io.File
import java.io.FileInputStream
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.*
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

/**
 * 例子：<br></br>
 * <pre>
 * URI uri = URI.create("wss://www.test.com/dev");
 * int port = uri.getPort();
 * if (port == -1) {
 * if (uri.getScheme().equals("wss")) {
 * port = 443;
 * } else {
 * port = 80;
 * }
 * }
 * SSLContext content = SSLEngineUtils.getSSLContext();
 * SSLEngine sslEngine = content.createSSLEngine(uri.getHost(), port);
 * sslEngine.setUseClientMode(true);
 * sslEngine.setNeedClientAuth(false);
 * pipeline.addFirst("ssl", new SslHandler(sslEngine));
</pre> *
 */
object SSLEngineUtils {
    /**
     * 证书路径 可以是文件夹或者文件 证书格式 *.cer
     */
    var cerPath: String = "data/cer"
    var TLS: String = "TLSv1.2"

    val sSLContext: SSLContext
        get() {
            val keyStore = buildKeyStore()
            val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            tmf.init(keyStore)
            val sslContext = if (TLS.isBlank()) SSLContext.getDefault() else SSLContext.getInstance(TLS)
            sslContext.init(null, tmf.trustManagers, null)
            return sslContext
        }

    private fun buildKeyStore(): KeyStore {
        // 获取自签名的证书
        val certs = loadHttpsCerts()
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        keyStore.load(null, null)
        val trustManagerFactory = TrustManagerFactory.getInstance(
            TrustManagerFactory.getDefaultAlgorithm()
        )
        trustManagerFactory.init(null as KeyStore?)
        // 获得jdk证书库中的证书
        Arrays.stream(trustManagerFactory.trustManagers)
            .filter { manager -> manager is X509TrustManager }
            .findFirst()
            .ifPresent { manager ->
                val jdkCerts = listOf(*(manager as X509TrustManager).acceptedIssuers)
                certs.addAll(jdkCerts)
            }
        certs.forEach { cert: X509Certificate ->
            keyStore.setCertificateEntry(cert.subjectX500Principal.name, cert)
        }
        return keyStore
    }

    private fun loadHttpsCerts(): MutableList<X509Certificate> {
        val certs = mutableListOf<X509Certificate>()
        val file = File(cerPath)
        if (file.isDirectory) {
            val fileList = file.listFiles()
            if (fileList != null) {
//                System.out.println("有cer=" + fileList.length);
                for (value in fileList) {
                    certs.add(loadCer(value))
                }
            }
        } else {
            certs.add(loadCer(file))
        }
        return certs
    }

    private fun loadCer(file: File): X509Certificate {
        val inputStream = FileInputStream(file)
        val factory = CertificateFactory.getInstance("X.509")
        return factory.generateCertificate(inputStream) as X509Certificate
    }
}

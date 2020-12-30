package com.onenet.studio.acc.sdk.util;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * @author fanhaiqiu
 * @date 2019/5/27
 */
public class SslUtil {

    /**
     * ssl
     *
     * @param caCrtFile ssl证书
     * @return
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws KeyStoreException
     * @throws CertificateException
     * @throws KeyManagementException
     */
    public static SSLSocketFactory getSocketFactory(byte[] caCrtFile) throws NoSuchAlgorithmException,
            IOException, KeyStoreException, CertificateException, KeyManagementException {
        Security.addProvider(new BouncyCastleProvider());
        //===========加载 ca 证书==================================
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        if (null != caCrtFile) {
            // 加载本地指定的 ca 证书
            PEMReader reader = new PEMReader(new InputStreamReader(new ByteArrayInputStream(caCrtFile)));
            X509Certificate caCert = (X509Certificate) reader.readObject();
            reader.close();
            // CA certificate is used to authenticate server
            KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
            caKs.load(null, null);
            caKs.setCertificateEntry("ca-certificate", caCert);
            // 把ca作为信任的 ca 列表,来验证服务器证书
            tmf.init(caKs);
        } else {
            //使用系统默认的安全证书
            tmf.init((KeyStore) null);
        }
        // ============finally, create SSL socket factory==============
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, tmf.getTrustManagers(), null);
        return context.getSocketFactory();
    }

}

package com.onenet.studio.acc.sdk.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * @author: fanhaiqiu
 * @date: 2020/12/22
 */
public class EncryptUtil {

    /**
     * 签名加密算法
     */
    public static enum SignatureMethod {
        SHA1, MD5, SHA256;
    }

    /**
     * 生成token
     *
     * @param version
     * @param resourceName
     * @param expirationTime
     * @param signatureMethod
     * @param accessKey
     * @return
     * @throws UnsupportedEncodingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    public static String assembleToken(String version, String resourceName, String expirationTime, String signatureMethod, String accessKey)
            throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
        StringBuilder sb = new StringBuilder();
        String res = URLEncoder.encode(resourceName, "UTF-8");
        String sig = URLEncoder.encode(generatorSignature(version, resourceName, expirationTime, accessKey, signatureMethod), "UTF-8");
        sb.append("version=")
                .append(version)
                .append("&res=")
                .append(res)
                .append("&et=")
                .append(expirationTime)
                .append("&method=")
                .append(signatureMethod)
                .append("&sign=")
                .append(sig);
        return sb.toString();
    }

    /**
     * 生成签名
     *
     * @param version
     * @param resourceName
     * @param expirationTime
     * @param accessKey
     * @param signatureMethod
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    public static String generatorSignature(String version, String resourceName, String expirationTime, String accessKey, String signatureMethod)
            throws NoSuchAlgorithmException, InvalidKeyException {
        String encryptText = expirationTime + "\n" + signatureMethod + "\n" + resourceName + "\n" + version;
        String signature;
        byte[] bytes = HmacEncrypt(encryptText, accessKey, signatureMethod);
        signature = Base64.getEncoder().encodeToString(bytes);
        return signature;
    }

    /**
     * mac 加密
     *
     * @param data
     * @param key
     * @param signatureMethod
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    public static byte[] HmacEncrypt(String data, String key, String signatureMethod) throws NoSuchAlgorithmException, InvalidKeyException {
        //根据给定的字节数组构造一个密钥,第二参数指定一个密钥算法的名称
        SecretKeySpec signinKey = null;
        signinKey = new SecretKeySpec(Base64.getDecoder().decode(key), "Hmac" + signatureMethod.toUpperCase());
        //生成一个指定 Mac 算法 的 Mac 对象
        Mac mac = null;
        mac = Mac.getInstance("Hmac" + signatureMethod.toUpperCase());
        //用给定密钥初始化 Mac 对象
        mac.init(signinKey);
        //完成 Mac 操作
        return mac.doFinal(data.getBytes());
    }
}

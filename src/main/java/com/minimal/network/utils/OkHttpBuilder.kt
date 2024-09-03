package com.minimal.network.utils

import com.minimal.network.NetConfig
import com.minimal.network.interceptor.RequestInterceptor
import okhttp3.OkHttpClient
import java.io.InputStream
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * 开启网络请求加密和响应校验
 * @param isEncrypt 是否开启d请求加密和响应校验, 默认为true
 */
fun OkHttpClient.Builder.setEncrypt(isEncrypt: Boolean = false) = apply {
    NetConfig.isEncrypt = isEncrypt
}

/**
 * 添加轻量级的请求拦截器, 可以在每次请求之前修改参数或者客户端配置
 * 该拦截器不同于OkHttp的Interceptor无需处理请求动作
 */
fun OkHttpClient.Builder.setRequestInterceptor(interceptor: RequestInterceptor) = apply {
    NetConfig.requestInterceptorList.add(interceptor)
}

/**
 * 配置信任所有证书
 * @param trustManager 如果需要自己校验，那么可以自己实现相关校验，如果不需要自己校验，那么传null即可
 * @param bksFile  客户端使用bks证书校验服务端证书
 * @param password bks证书的密码
 */
fun OkHttpClient.Builder.setSSLCertificate(
    trustManager: X509TrustManager?,
    bksFile: InputStream? = null,
    password: String? = null,
) = apply {
    try {
        val trustManagerFinal: X509TrustManager = trustManager ?: Https.UnSafeTrustManager
        val keyManagers = prepareKeyManager(bksFile, password)
        val sslContext = SSLContext.getInstance("TLS")
        // 用上面得到的trustManagers初始化SSLContext，这样sslContext就会信任keyStore中的证书
        // 第一个参数是授权的密钥管理器，用来授权验证，比如授权自签名的证书验证。第二个是被授权的证书管理器，用来验证服务器端的证书
        sslContext.init(keyManagers, arrayOf<TrustManager?>(trustManagerFinal), null)
        // 通过sslContext获取SSLSocketFactory对象
        sslSocketFactory(sslContext.socketFactory, trustManagerFinal)
    } catch (e: NoSuchAlgorithmException) {
        throw AssertionError(e)
    } catch (e: KeyManagementException) {
        throw AssertionError(e)
    }
}

/**
 * 配置信任所有证书
 * @param certificates 含有服务端公钥的证书校验服务端证书
 * @param bksFile  客户端使用bks证书校验服务端证书
 * @param password bks证书的密码
 */
fun OkHttpClient.Builder.setSSLCertificate(
    vararg certificates: InputStream,
    bksFile: InputStream? = null,
    password: String? = null
) = apply {
    val trustManager = prepareTrustManager(*certificates)?.let { chooseTrustManager(it) }
    setSSLCertificate(trustManager, bksFile, password)
}

/**
 * 信任所有证书
 */
fun OkHttpClient.Builder.trustSSLCertificate() = apply {
    hostnameVerifier(Https.UnSafeHostnameVerifier)
    setSSLCertificate(null)
}
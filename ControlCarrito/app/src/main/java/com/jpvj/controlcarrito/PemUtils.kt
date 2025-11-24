package com.jpvj.controlcarrito

import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.io.InputStream
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.Security
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*

object PemUtils {

    fun getPrivateKey(inputStream: InputStream): PrivateKey {
        Security.addProvider(BouncyCastleProvider())

        val pem = inputStream.readBytes().toString(Charsets.UTF_8)
            .replace("-----BEGIN RSA PRIVATE KEY-----", "")
            .replace("-----END RSA PRIVATE KEY-----", "")
            .replace("\\s".toRegex(), "")

        val encoded = Base64.getDecoder().decode(pem)
        val keySpec = PKCS8EncodedKeySpec(encoded)

        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePrivate(keySpec)
    }
}

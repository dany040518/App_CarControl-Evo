package com.jpvj.controlcarrito

import android.content.Context
import java.io.BufferedInputStream
import java.io.InputStream
import java.security.KeyStore
import javax.net.ssl.*

object SslSocketFactory {

    fun getSocketFactory(context: Context): SSLSocketFactory {
        val caInput: InputStream = context.resources.openRawResource(R.raw.aws_root_ca)
        val crtInput: InputStream = context.resources.openRawResource(R.raw.aws_certificate)
        val keyInput: InputStream = context.resources.openRawResource(R.raw.aws_private)

        // Cargar CA
        val cf = java.security.cert.CertificateFactory.getInstance("X.509")
        val ca = cf.generateCertificate(BufferedInputStream(caInput))

        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        keyStore.load(null, null)
        keyStore.setCertificateEntry("ca", ca)

        // Certificado + llave privada en formato PKCS12
        val p12KeyStore = KeyStore.getInstance("PKCS12")
        p12KeyStore.load(null, null)
        p12KeyStore.setKeyEntry(
            "private-key",
            PemUtils.getPrivateKey(keyInput),
            null,
            arrayOf(cf.generateCertificate(crtInput))
        )

        val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
        kmf.init(p12KeyStore, null)

        val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        tmf.init(keyStore)

        val sslContext = SSLContext.getInstance("TLSv1.2")
        sslContext.init(kmf.keyManagers, tmf.trustManagers, null)

        return sslContext.socketFactory
    }
}

package com.yuchens.equipinspectandroid.util

import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object AESHelper {
    const val CRYPTO_KEY: String = "YCS"

    /** 與 .NET 相容：Key = SHA256(YCS)，IV = MD5(YCS)，AES/CBC/PKCS5Padding */
    fun encrypt(plain: String, keyStr: String = CRYPTO_KEY): String {
        val key = sha256(keyStr)
        val iv = md5(keyStr)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val secretKey = SecretKeySpec(key, "AES")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(iv))
        val encrypted = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
        return android.util.Base64.encodeToString(encrypted, android.util.Base64.NO_WRAP)
    }

    private fun sha256(s: String): ByteArray =
        MessageDigest.getInstance("SHA-256").digest(s.toByteArray(Charsets.UTF_8))

    private fun md5(s: String): ByteArray =
        MessageDigest.getInstance("MD5").digest(s.toByteArray(Charsets.UTF_8))
}

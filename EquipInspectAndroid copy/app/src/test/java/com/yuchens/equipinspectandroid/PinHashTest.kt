package com.yuchens.equipinspectandroid

import org.junit.Test
import java.security.MessageDigest

class PinHashTest {

    /** 計算 SHA-256 → 十六進位字串 */
    private fun sha256Hex(s: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest(s.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * 用「硬編 PIN」快速算一次（只是臨時用來產生 hash）
     */
    @Test
    fun printPinHash_quick() {
        val pepper = "YCS-Pepper"
        val pin = ""   // 替換成實際的 PIN
        val hash = sha256Hex("$pepper:$pin")
        println("SHA-256(pepper:pin) = $hash")
    }
}

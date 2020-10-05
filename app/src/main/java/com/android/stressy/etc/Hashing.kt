package com.android.stressy.etc

import java.io.ByteArrayInputStream
import java.security.DigestInputStream
import java.security.MessageDigest

class Hashing {
    companion object{
        fun calculateHash(str: String, algorithm: String = "SHA-256"): String {
            val digest = MessageDigest.getInstance(algorithm)
            val digestStream = DigestInputStream(ByteArrayInputStream(str.toByteArray()) , digest)
            while (digestStream.read() != -1) {
                // The DigestInputStream does the work; nothing for us to do.
            }
            return digest.digest().joinToString(":") { "%02x".format(it) }
        }
    }
}
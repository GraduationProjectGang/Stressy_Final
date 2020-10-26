package com.android.stressy.paillier

import java.math.BigInteger
import java.security.SecureRandom
import java.util.*

class KeyPairBuilder {

    private var bits: Int = 1024
    private var certainty: Int = 0
    private var rng: Random? = null
    private var upperBound: BigInteger? = null

    public fun bits(bits: Int): KeyPairBuilder {
        this.bits = bits
        return this
    }

    public fun certainty(certainty: Int): KeyPairBuilder {
        this.certainty = certainty
        return this
    }

    public fun randomNumGenerator(rng: Random): KeyPairBuilder {
        this.rng = rng
        return this
    }

    public fun upperBound(b: BigInteger): KeyPairBuilder {
        this.upperBound = b
        return this
    }

    public fun generateKeyPair(): KeyPair {
        if (rng == null) {
            rng = SecureRandom()
        }

        lateinit var p: BigInteger
        lateinit var q: BigInteger

        val length: Int = bits / 2
        if (certainty > 0) {
            p = BigInteger(length, certainty, rng)
            q = BigInteger(length, certainty, rng)
        }
        else {
            p = BigInteger(length, rng)
            q = BigInteger(length, rng)
        }
        val n: BigInteger = p.multiply(q)
        val nSquared: BigInteger = n.multiply(n) // n^2

        val pMinusOne: BigInteger = p.subtract(BigInteger.ONE)
        val qMinusOne: BigInteger = q.subtract(BigInteger.ONE)

        val lambda: BigInteger = this.lcm(pMinusOne, qMinusOne)

        lateinit var g: BigInteger
        lateinit var helper: BigInteger

        do {
            g = BigInteger(bits, rng)
            helper = calculateL(g.modPow(lambda, nSquared), n)
        } while (helper.gcd(n) != BigInteger.ONE)

        val publicKey = PublicKey(n, nSquared, g, bits)
        val privateKey = PrivateKey(lambda, helper.modInverse(n))

        return KeyPair(privateKey, publicKey, upperBound)
    }

    private fun calculateL(u: BigInteger, n: BigInteger): BigInteger {
        var result = u.subtract(BigInteger.ONE)
        result = result.divide(n)
        return result
    }

    private fun lcm(a: BigInteger, b: BigInteger): BigInteger {
        lateinit var result: BigInteger
        val gcd = a.gcd(b)
        result = a.abs().divide(gcd)
        result = result.multiply(b.abs())
        return result
    }



}
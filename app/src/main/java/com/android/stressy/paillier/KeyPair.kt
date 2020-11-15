package com.android.stressy.paillier

import java.math.BigInteger
import java.util.*

class KeyPair() {

    private val CERTAINTY = 64 // certainty with which primes are generated: 1-2^(-CERTAINTY)
    val modLength: Int = 1024// length in bits of the modulus n

    private lateinit var lambda: BigInteger
    private lateinit var n: BigInteger
    private lateinit var nSquared: BigInteger
    private lateinit var g: BigInteger
    private lateinit var mu: BigInteger

    init {
        generateKeys()
    }

    constructor(n: BigInteger, g: BigInteger, lambda: BigInteger, mu: BigInteger) : this() {
        this.n = n
        this.g = g
        this.lambda = lambda
        this.mu = mu
        this.nSquared = n.multiply(n)
    }

    private fun generateKeys() {

        val p = BigInteger(modLength / 2, CERTAINTY, Random()) // a random prime
        lateinit var q: BigInteger
        do {
            q = BigInteger(modLength / 2, CERTAINTY, Random()) // a random prime (distinct from p)
        } while (q.compareTo(p) == 0)

        lambda = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE)).divide(
            p.subtract(BigInteger.ONE).gcd(q.subtract(BigInteger.ONE))
        )
        n = p.multiply(q)

        nSquared = n.multiply(n)
        do {
            g = randomZStarNSquare()
        } while (g.modPow(lambda, nSquared).subtract(BigInteger.ONE).divide(n).gcd(n) != BigInteger.ONE)

        mu = g.modPow(lambda, nSquared).subtract(BigInteger.ONE).divide(n).modInverse(n)

    }

    fun encrypt(m: BigInteger): BigInteger {

        if (m < BigInteger.ZERO || m >= n) {
            return BigInteger.ZERO
        }

        val r: BigInteger = randomZStarN()
        return g.modPow(m, nSquared).multiply(r.modPow(n, nSquared)).mod(nSquared)
    }

    fun decrypt(c: BigInteger): BigInteger {
        return c.modPow(lambda, nSquared).subtract(BigInteger.ONE).divide(n).multiply(mu).mod(n)
    }

    private fun randomZStarN(): BigInteger {
        var r: BigInteger
        do {
            r = BigInteger(modLength, Random())
        } while (r >= n || r.gcd(n) != BigInteger.ONE)
        return r
    }

    private fun randomZStarNSquare(): BigInteger {
        var r: BigInteger
        do {
            r = BigInteger(modLength * 2, Random())
        } while (r >= nSquared || r.gcd(nSquared) != BigInteger.ONE)
        return r
    }

    fun getLambda(): BigInteger {
        return lambda
    }

    fun getN(): BigInteger {
        return n
    }

    fun getnSquared(): BigInteger {
        return nSquared
    }

    fun getG(): BigInteger {
        return g
    }

    fun getMu(): BigInteger {
        return mu
    }

}
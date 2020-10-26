package com.android.stressy.paillier

import java.math.BigInteger

class KeyPair(privateKey: PrivateKey, publicKey: PublicKey, upperBound: BigInteger?) {

    private val privateKey: PrivateKey = privateKey
    private val publicKey: PublicKey = publicKey
    private val upperBound: BigInteger? = upperBound

    public fun getPrivateKey(): PrivateKey {
        return privateKey
    }

    public fun getPublicKey(): PublicKey {
        return publicKey
    }

    public fun decrypt(c: BigInteger): BigInteger {
        val n: BigInteger = publicKey.getN()
        val nSquare: BigInteger = publicKey.getnSquared()
        val lambda: BigInteger = privateKey.getLambda()

        val u: BigInteger = privateKey.getPreCalculatedDenominator()
        var p: BigInteger = c.modPow(lambda, nSquare).subtract(BigInteger.ONE).divide(n).multiply(u).mod(n)

        if (upperBound != null && p > upperBound) {
            p = p.subtract(n)
        }

        return p
    }

}
package com.android.stressy.paillier

import java.math.BigInteger
import java.util.*

class PublicKey(n: BigInteger, nSquared: BigInteger, g: BigInteger, bits: Int) {

    private val bits: Int = bits
    private val n: BigInteger = n
    private val nSquared: BigInteger = nSquared
    private val g: BigInteger = g

    public fun getBits(): Int {
        return bits
    }

    public fun getN(): BigInteger {
        return n
    }

    public fun getnSquared(): BigInteger {
        return nSquared
    }

    public fun getG(): BigInteger {
        return g
    }

    public fun encrypt(m: BigInteger): BigInteger {
        lateinit var r: BigInteger
        do {
            r = BigInteger(bits, Random())
        } while (r >= n)

        var result: BigInteger = g.modPow(m, nSquared)
        val x: BigInteger = r.modPow(n, nSquared)

        result = result.multiply(x)
        result = result.mod(nSquared)

        return result
    }

}
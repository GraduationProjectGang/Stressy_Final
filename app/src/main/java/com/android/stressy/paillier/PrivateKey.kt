package com.android.stressy.paillier

import java.math.BigInteger

class PrivateKey(lambda: BigInteger, preCalculatedDenominator: BigInteger) {

    private val lambda: BigInteger = lambda
    private val preCalculatedDenominator: BigInteger = preCalculatedDenominator

    public fun getLambda(): BigInteger {
        return lambda
    }

    public fun getPreCalculatedDenominator(): BigInteger {
        return preCalculatedDenominator
    }

}
package com.example.data.config

object NativeTokenConfig {
    const val TOKEN_NAME = "CDF Token"
    const val TOKEN_SYMBOL = "CDF"
    const val CONTRACT_ADDRESS = "0x18e173fdeb700568a08d1d7049309ae322d27777"
    const val ADMIN_FEE_ADDRESS = "0x0E9dBe33a4fb33Fc9e6595A154538D721965401b"
    const val PROTOCOL_FEE_PERCENT = 0.02 // 2% fee on buys, sells, swaps, and transfers
    const val NETWORK_NAME = "BNB Smart Chain / Base EVM"
    const val DECIMALS = 18

    fun calculateFee(amount: Double): Double {
        return amount * PROTOCOL_FEE_PERCENT
    }

    fun shortenAddress(address: String): String {
        return if (address.length > 12) {
            "${address.take(6)}...${address.takeLast(4)}"
        } else {
            address
        }
    }
}

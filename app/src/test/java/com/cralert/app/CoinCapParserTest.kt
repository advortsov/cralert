package com.cralert.app

import com.cralert.app.data.remote.CoinCapParser
import org.junit.Assert.assertEquals
import org.junit.Test

class CoinCapParserTest {

    @Test
    fun parsesAssetsFromJson() {
        val json = """
            {
              "data": [
                {"id":"bitcoin","symbol":"BTC","name":"Bitcoin","priceUsd":"123.45"},
                {"id":"ethereum","symbol":"ETH","name":"Ethereum","priceUsd":"67.89"}
              ]
            }
        """.trimIndent()

        val assets = CoinCapParser.parseAssets(json)

        assertEquals(2, assets.size)
        assertEquals("bitcoin", assets[0].id)
        assertEquals(123.45, assets[0].priceUsd, 0.0001)
    }
}

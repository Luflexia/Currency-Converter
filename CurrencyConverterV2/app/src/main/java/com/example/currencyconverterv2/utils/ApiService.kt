package com.example.currencyconverterv2.utils

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

data class BelarusbankRate(
    val curAbbreviation: String,
    val rate: Double
)

class ApiService {

    private val client = OkHttpClient()

    // временно
    fun getExchangeRatesFromBelarusbank(): List<BelarusbankRate> {
        return listOf(
            BelarusbankRate("USD", 1.0),    // Assume USD as base
            BelarusbankRate("EUR", 0.85),   // EUR to USD
            BelarusbankRate("RUB", 70.0),   // RUB to USD
            BelarusbankRate("BYN", 2.5),    // BYN to USD
            BelarusbankRate("GBP", 0.75),   // GBP to USD
            BelarusbankRate("JPY", 110.0)   // JPY to USD
        )
    }
}

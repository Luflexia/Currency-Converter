package com.example.currencyconverterv2.utils

import com.example.currencyconverterv2.models.Bank
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import kotlin.random.Random

data class BelarusbankRate(
    val curAbbreviation: String,
    val rate: Double
)

class ApiService {
    private val client = OkHttpClient()

    companion object {
        val banks = listOf(
            Bank("Национальный банк", "NBRB"),
            Bank("Беларусбанк", "BBANK"),
            Bank("Белагропромбанк", "BAGRO"),
            Bank("БПС-Сбербанк", "BPS"),
            Bank("Белинвестбанк", "BINVEST"),
            Bank("Белгазпромбанк", "BGPB"),
            Bank("Альфа-Банк", "ALPHA")
        )
    }

    fun getExchangeRates(bankCode: String): List<BelarusbankRate> {
        return when (bankCode) {
            "NBRB" -> getExchangeRatesNBRB()
            else -> getFixedExchangeRates(bankCode)
        }
    }

    // Изменено с private на public
    fun getExchangeRatesNBRB(): List<BelarusbankRate> {
        val request = Request.Builder()
            .url("https://api.nbrb.by/exrates/rates?periodicity=0")
            .build()

        val response = client.newCall(request).execute()
        val jsonString = response.body?.string()

        if (jsonString != null) {
            val jsonArray = JSONArray(jsonString)
            val rates = mutableListOf<BelarusbankRate>()
            var usdRate = 1.0
            // Сначала найдем курс USD
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                if (item.getString("Cur_Abbreviation") == "USD") {
                    usdRate = item.getDouble("Cur_OfficialRate") / item.getInt("Cur_Scale")
                    break
                }
            }

            // Теперь обработаем все валюты
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                val curAbbreviation = item.getString("Cur_Abbreviation")
                val curScale = item.getInt("Cur_Scale")
                val curOfficialRate = item.getDouble("Cur_OfficialRate")

                // Рассчитываем курс относительно USD
                val rate = usdRate / (curOfficialRate / curScale)

                rates.add(BelarusbankRate(curAbbreviation, rate))
            }

            rates.add(BelarusbankRate("BYN", usdRate))

            return rates
        } else {
            throw Exception("Failed to fetch exchange rates")
        }
    }

    private fun getFixedExchangeRates(bankCode: String): List<BelarusbankRate> {
        // Get the base rates from NBRB
        val baseRates = getExchangeRatesNBRB().associateBy { it.curAbbreviation }

        // Generate small deviations for each bank
        val random = Random(bankCode.hashCode())
        return baseRates.map { (currency, belarusbankRate) ->
            val deviation = random.nextDouble(-0.05, 0.05)
            BelarusbankRate(currency, belarusbankRate.rate * (1 + deviation))
        }
    }
}
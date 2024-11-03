package com.example.currencyconverterv2.activities

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.currencyconverterv2.adapters.CurrencyAdapter
import com.example.currencyconverterv2.databinding.ActivityMainBinding
import com.example.currencyconverterv2.models.Currency
import com.example.currencyconverterv2.utils.ApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.currencyconverterv2.R
import com.example.currencyconverterv2.utils.CurrencyGestureHelper
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var currencyAdapter: CurrencyAdapter
    private var currencies: MutableList<Currency> = mutableListOf()
    private var selectedCurrencies: MutableList<Currency> = mutableListOf() // Список выбранных валют
    private val apiService = ApiService()

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // View Binding initialization
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    // Проверяем, является ли устройство планшетом
    if (resources.getBoolean(R.bool.is_tablet)) {
        // Если это планшет, разрешаем поворот экрана
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    } else {
        // Если это телефон, фиксируем ориентацию в портретном режиме
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    // RecyclerView setup
    currencyAdapter = CurrencyAdapter(selectedCurrencies) { currencyName, inputAmount ->
        updateCurrencies(currencyName, inputAmount)
    }

    binding.currencyRecyclerView.layoutManager = LinearLayoutManager(this)
    binding.currencyRecyclerView.adapter = currencyAdapter

    // Использование CurrencyGestureHelper для жестов
    val gestureHelper = CurrencyGestureHelper(currencyAdapter)
    val itemTouchHelper = ItemTouchHelper(gestureHelper)
    itemTouchHelper.attachToRecyclerView(binding.currencyRecyclerView)

    // Load exchange rates
    loadExchangeRates()

    // Setup "Add Currency" button
    binding.addCurrencyButton.setOnClickListener {
        showCurrencySelectionDialog()
    }
}

    private fun loadExchangeRates() {
        CoroutineScope(Dispatchers.IO).launch {
            val exchangeRates = apiService.getExchangeRatesFromBelarusbank() // Всегда возвращает список
            currencies.clear()

            // Добавляем валюты и автоматически активируем USD, EUR, BYN, RUB
            for (rate in exchangeRates) {
                val currency = Currency(rate.curAbbreviation, rate.rate)
                currencies.add(currency)

                // Добавляем USD, EUR, BYN, RUB в список выбранных валют по умолчанию
                if (currency.name in listOf("USD", "EUR", "BYN", "RUB")) {
                    selectedCurrencies.add(currency)
                }
            }
            runOnUiThread {
                currencyAdapter.notifyDataSetChanged() // Обновляем адаптер после загрузки данных
            }
        }
    }

    private fun updateCurrencies(inputCurrency: String, inputAmount: Double) {
        currencyAdapter.updateCurrencies(inputCurrency, inputAmount)
    }

    private fun showCurrencySelectionDialog() {
        val currencyNames = currencies.map { it.name }.toTypedArray()

        // Инициализируем массив выбранных валют
        val selected = BooleanArray(currencyNames.size) { index ->
            selectedCurrencies.any { it.name == currencyNames[index] }
        }

        // Открываем диалог с чекбоксами
        AlertDialog.Builder(this)
            .setTitle("Выберите валюты")
            .setMultiChoiceItems(currencyNames, selected) { _, index, isChecked ->
                val currency = currencies[index]
                if (isChecked) {
                    addCurrency(currency)
                } else {
                    removeCurrency(currency)
                }
            }
            .setPositiveButton("OK") { dialog, _ ->
                currencyAdapter.notifyDataSetChanged() // Обновляем список после выбора
                dialog.dismiss()
            }
            .show()
    }

    private fun addCurrency(currency: Currency) {
        if (!selectedCurrencies.contains(currency)) {
            selectedCurrencies.add(currency)
            updateCurrencies(currency.name, currency.convertedValue)
        }
    }

    private fun removeCurrency(currency: Currency) {
        selectedCurrencies.remove(currency)
        updateCurrencies(currency.name, 0.0)
    }
}

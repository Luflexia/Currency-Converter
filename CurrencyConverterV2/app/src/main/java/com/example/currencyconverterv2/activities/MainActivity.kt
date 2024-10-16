package com.example.currencyconverterv2.activities

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.currencyconverterv2.adapters.CurrencyAdapter
import com.example.currencyconverterv2.databinding.ActivityMainBinding
import com.example.currencyconverterv2.models.Currency
import com.example.currencyconverterv2.utils.ApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var currencyAdapter: CurrencyAdapter
    private var currencies: MutableList<Currency> = mutableListOf()
    private var selectedCurrencies: MutableList<Currency> = mutableListOf()
    private val apiService = ApiService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // View Binding initialization
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // RecyclerView setup
        currencyAdapter = CurrencyAdapter(currencies)

        binding.currencyRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.currencyRecyclerView.adapter = currencyAdapter

        // Подключение ItemTouchHelper для обработки перемещения и удаления
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding.currencyRecyclerView)

        // Load exchange rates
        loadExchangeRates()

        // Setup "Add Currency" button
        binding.addCurrencyButton.setOnClickListener {
            showCurrencySelectionDialog()
        }
    }

    private val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN, // Для перемещения
        ItemTouchHelper.LEFT // Для удаления
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val fromPosition = viewHolder.adapterPosition
            val toPosition = target.adapterPosition
            currencyAdapter.moveCurrency(fromPosition, toPosition) // Перемещение валюты
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            currencyAdapter.removeCurrency(position) // Удаление валюты
        }
    }

    private fun loadExchangeRates() {
        CoroutineScope(Dispatchers.IO).launch {
            val exchangeRates = apiService.getExchangeRatesFromBelarusbank()
            if (exchangeRates != null) {
                currencies.clear()
                for (rate in exchangeRates) {
                    val currency = Currency(
                        rate.curAbbreviation,
                        rate.rate
                    )
                    currencies.add(currency)
                }
                runOnUiThread {
                    currencyAdapter.notifyDataSetChanged()
                }
            }
        }
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
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
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

    private fun updateCurrencies(inputCurrency: String, inputAmount: Double) {
        val inputRate = currencies.find { it.name == inputCurrency }?.rate ?: return

        for (currency in currencies) {
            if (currency.name != inputCurrency) {
                val convertedAmount = inputAmount / inputRate * currency.rate
                currency.convertedValue = convertedAmount
            } else {
                currency.convertedValue = inputAmount
            }
        }
        runOnUiThread {
            currencyAdapter.notifyDataSetChanged()
        }
    }
}

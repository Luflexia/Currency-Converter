package com.example.currencyconverterv2.activities

import android.content.Intent
import android.content.pm.ActivityInfo // Add this import
import android.os.Bundle
import android.util.Log
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
import android.widget.Toast

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var currencyAdapter: CurrencyAdapter
    private var currencies: MutableList<Currency> = mutableListOf()
    private var selectedCurrencies: MutableList<Currency> = mutableListOf()
    private val apiService = ApiService()
    private var selectedBankCode: String = "NBRB" // Default bank code

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ставим альбомную ориентацию экрана, если устройство планшет,если нет, то портретную ориентацию.
        if (resources.getBoolean(R.bool.is_tablet)) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        } else {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        currencyAdapter = CurrencyAdapter(selectedCurrencies) { currencyName, inputAmount ->
            updateCurrencies(currencyName, inputAmount)
        }
        binding.currencyRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.currencyRecyclerView.adapter = currencyAdapter

        val gestureHelper = CurrencyGestureHelper(currencyAdapter)
        val itemTouchHelper = ItemTouchHelper(gestureHelper)
        itemTouchHelper.attachToRecyclerView(binding.currencyRecyclerView)

        loadExchangeRates(selectedBankCode)

        binding.addCurrencyButton.setOnClickListener {
            showCurrencySelectionDialog()
        }

        binding.logoutButton.setOnClickListener {
            logout()
        }

        binding.selectBankButton.setOnClickListener {
            selectBank()
        }
    }

    private fun loadExchangeRates(bankCode: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val exchangeRates = apiService.getExchangeRates(bankCode)
                currencies.clear()
                selectedCurrencies.clear() // Clear the selected currencies to avoid duplicates

                for (rate in exchangeRates) {
                    val currency = Currency(rate.curAbbreviation, rate.rate)
                    currencies.add(currency)

                    if (currency.name in listOf("BYN", "USD", "EUR", "RUB")) {
                        selectedCurrencies.add(currency)
                    }
                }
                runOnUiThread {
                    currencyAdapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error loading exchange rates: ${e.message}", e)
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Ошибка загрузки курсов: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun selectBank() {
        val intent = Intent(this, BankSelectionActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_SELECT_BANK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SELECT_BANK && resultCode == RESULT_OK) {
            data?.let {
                selectedBankCode = it.getStringExtra("selectedBankCode") ?: "NBRB"
                loadExchangeRates(selectedBankCode)
            }
        }
    }

    private fun logout() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun updateCurrencies(inputCurrency: String, inputAmount: Double) {
        currencyAdapter.updateCurrencies(inputCurrency, inputAmount)
    }

    private fun showCurrencySelectionDialog() {
        val currencyNames = currencies.map { it.name }.toTypedArray()

        val selected = BooleanArray(currencyNames.size) { index ->
            selectedCurrencies.any { it.name == currencyNames[index] }
        }

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
                currencyAdapter.notifyDataSetChanged()
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

    companion object {
        private const val REQUEST_CODE_SELECT_BANK = 1
    }
}
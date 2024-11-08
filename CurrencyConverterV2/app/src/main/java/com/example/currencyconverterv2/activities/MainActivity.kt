package com.example.currencyconverterv2.activities

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.currencyconverterv2.R
import com.example.currencyconverterv2.adapters.CurrencyAdapter
import com.example.currencyconverterv2.databinding.ActivityMainBinding
import com.example.currencyconverterv2.models.Currency
import com.example.currencyconverterv2.utils.ApiService
import com.example.currencyconverterv2.utils.CurrencyGestureHelper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var currencyAdapter: CurrencyAdapter
    private var currencies: MutableList<Currency> = mutableListOf()
    private var selectedCurrencies: MutableList<Currency> = mutableListOf()
    private val apiService = ApiService()
    private var selectedBankCode: String = "NBRB" // Default bank code
    private var selectedBankName: String = "Национальный банк" // Default bank name
    private lateinit var currentUsername: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Получаем имя пользователя из SharedPreferences
        val sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE)
        currentUsername = sharedPreferences.getString("CURRENT_USER", "") ?: ""

        if (currentUsername.isEmpty()) {
            // Если имя пользователя не найдено, возвращаемся к экрану входа
            Toast.makeText(this, "Пожалуйста, войдите в систему", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        Toast.makeText(this, "Добро пожаловать, $currentUsername", Toast.LENGTH_LONG).show()

        // Set screen orientation based on device type
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

        // Перенесен вызов loadSelectedCurrencies() сюда
        loadSelectedCurrencies()

        binding.addCurrencyButton.setOnClickListener {
            showCurrencySelectionDialog()
        }

        binding.logoutButton.setOnClickListener {
            saveSelectedCurrencies()
            getSharedPreferences("LoginPrefs", MODE_PRIVATE).edit().remove("CURRENT_USER").apply()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.selectBankButton.setOnClickListener {
            selectBank()
        }

        updateBankDisplay()
    }

    override fun onPause() {
        super.onPause()
        saveSelectedCurrencies()
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
                }
                runOnUiThread {
                    loadSelectedCurrencies()
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
                selectedBankName = it.getStringExtra("selectedBankName") ?: "Национальный банк"
                loadExchangeRates(selectedBankCode)
                updateBankDisplay()
            }
        }
    }

    private fun updateBankDisplay() {
        binding.selectedBankName.text = selectedBankName
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
                saveSelectedCurrencies()
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
    }

    private fun loadSelectedCurrencies() {
        try {
            val file = File(filesDir, "${currentUsername}_selected_currencies.json")
            if (file.exists()) {
                val json = file.readText()
                val type = object : TypeToken<List<String>>() {}.type
                val selectedCurrencyNames = Gson().fromJson<List<String>>(json, type)

                selectedCurrencies.clear()

                for (name in selectedCurrencyNames) {
                    currencies.find { it.name == name }?.let {
                        selectedCurrencies.add(it)
                    }
                }

                if (selectedCurrencies.isEmpty()) {
                    val defaultCurrencies = listOf("BYN", "USD", "EUR", "RUB")
                    for (name in defaultCurrencies) {
                        currencies.find { it.name == name }?.let {
                            selectedCurrencies.add(it)
                        }
                    }
                }

                currencyAdapter.notifyDataSetChanged()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка при загрузке валют: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun saveSelectedCurrencies() {
        try {
            val selectedCurrencyNames = selectedCurrencies.map { it.name }
            val json = Gson().toJson(selectedCurrencyNames)
            val file = File(filesDir, "${currentUsername}_selected_currencies.json")
            file.writeText(json)
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка при сохранении валют: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        const val REQUEST_CODE_SELECT_BANK = 1001
    }
}
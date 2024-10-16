package com.example.currencyconverterv2.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.currencyconverterv2.adapters.BankAdapter
import com.example.currencyconverterv2.databinding.ActivityBankSelectionBinding
import com.example.currencyconverterv2.models.Bank

class BankSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBankSelectionBinding
    private lateinit var bankAdapter: BankAdapter
    private var banks: MutableList<Bank> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализация View Binding
        binding = ActivityBankSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Предположим, что у вас есть какой-то список банков
        banks.add(Bank("National Bank", "NBRB"))
        banks.add(Bank("Bank ABC", "ABC"))
        banks.add(Bank("Bank XYZ", "XYZ"))

        bankAdapter = BankAdapter(banks) { selectedBank ->
            // Обработка выбора банка
            val intent = Intent()
            intent.putExtra("selectedBank", selectedBank.name)
            setResult(RESULT_OK, intent)
            finish()
        }

        binding.bankRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.bankRecyclerView.adapter = bankAdapter
    }
}

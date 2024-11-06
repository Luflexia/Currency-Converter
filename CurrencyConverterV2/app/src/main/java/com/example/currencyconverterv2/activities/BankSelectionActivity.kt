package com.example.currencyconverterv2.activities

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.currencyconverterv2.adapters.BankAdapter
import com.example.currencyconverterv2.databinding.ActivityBankSelectionBinding
import com.example.currencyconverterv2.models.Bank
import com.example.currencyconverterv2.utils.ApiService

class BankSelectionActivity : BaseActivity() {

    private lateinit var binding: ActivityBankSelectionBinding
    private lateinit var bankAdapter: BankAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBankSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        bankAdapter = BankAdapter(ApiService.banks) { selectedBank ->
            onBankSelected(selectedBank)
        }
        binding.bankRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@BankSelectionActivity)
            adapter = bankAdapter
        }
    }

    private fun onBankSelected(selectedBank: Bank) {
        val intent = Intent().apply {
            putExtra("selectedBankCode", selectedBank.code)
            putExtra("selectedBankName", selectedBank.name)
        }
        setResult(RESULT_OK, intent)
        finish()
    }
}
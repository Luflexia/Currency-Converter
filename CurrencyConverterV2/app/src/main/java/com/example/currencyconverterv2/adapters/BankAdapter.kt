package com.example.currencyconverterv2.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.currencyconverterv2.R
import com.example.currencyconverterv2.models.Bank

class BankAdapter(private val banks: List<Bank>, private val onBankSelected: (Bank) -> Unit) :
    RecyclerView.Adapter<BankAdapter.BankViewHolder>() {

    class BankViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bankName: TextView = itemView.findViewById(R.id.bankName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BankViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bank, parent, false)
        return BankViewHolder(view)
    }

    override fun onBindViewHolder(holder: BankViewHolder, position: Int) {
        val bank = banks[position]
        holder.bankName.text = bank.name
        holder.itemView.setOnClickListener {
            onBankSelected(bank) // Вызываем лямбда-функцию при выборе банка
        }
    }

    override fun getItemCount(): Int {
        return banks.size
    }
}

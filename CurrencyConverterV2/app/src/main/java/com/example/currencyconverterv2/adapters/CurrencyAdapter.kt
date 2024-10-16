package com.example.currencyconverterv2.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.currencyconverterv2.R
import com.example.currencyconverterv2.models.Currency

class CurrencyAdapter(private val currencies: MutableList<Currency>) :
    RecyclerView.Adapter<CurrencyAdapter.CurrencyViewHolder>() {

    // ViewHolder для каждого элемента списка
    class CurrencyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val currencyName: TextView = itemView.findViewById(R.id.currencyName)
        val currencyRate: TextView = itemView.findViewById(R.id.currencyRate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_currency, parent, false)
        return CurrencyViewHolder(view)
    }

    override fun onBindViewHolder(holder: CurrencyViewHolder, position: Int) {
        val currency = currencies[position]
        holder.currencyName.text = currency.name
        holder.currencyRate.text = currency.rate.toString()
    }

    override fun getItemCount(): Int {
        return currencies.size
    }

    // Перемещение валюты
    fun moveCurrency(fromPosition: Int, toPosition: Int) {
        val fromCurrency = currencies.removeAt(fromPosition)
        currencies.add(toPosition, fromCurrency)
        notifyItemMoved(fromPosition, toPosition)
    }

    // Удаление валюты
    fun removeCurrency(position: Int) {
        currencies.removeAt(position)
        notifyItemRemoved(position)
    }
}

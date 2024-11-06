package com.example.currencyconverterv2.adapters

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.currencyconverterv2.R
import com.example.currencyconverterv2.models.Currency
import java.text.DecimalFormat

class CurrencyAdapter(
    private val currencies: MutableList<Currency>,
    private val onAmountChanged: (String, Double) -> Unit
) : RecyclerView.Adapter<CurrencyAdapter.CurrencyViewHolder>() {

    private val decimalFormat = DecimalFormat("#.##")

    class CurrencyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val currencyName: TextView = itemView.findViewById(R.id.currencyName)
        val amountInput: EditText = itemView.findViewById(R.id.amountInput)
        val currencyFlag: ImageView = itemView.findViewById(R.id.currencyFlag)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_currency, parent, false)
        return CurrencyViewHolder(view)
    }

    override fun onBindViewHolder(holder: CurrencyViewHolder, position: Int) {
        val currency = currencies[position]
        holder.currencyName.text = currency.name
        setFlag(holder.currencyFlag, currency.name)

        // Удаляем предыдущий TextWatcher
        val previousWatcher = holder.amountInput.getTag(R.id.currencyAmountWatcher) as? TextWatcher
        previousWatcher?.let { holder.amountInput.removeTextChangedListener(it) }

        // Форматируем и устанавливаем текущее значение только если оно изменилось
        val formattedValue = formatAmountForDisplay(currency.convertedValue)
        if (holder.amountInput.text.toString() != formattedValue) {
            holder.amountInput.setText(formattedValue)
        }

        val watcher = object : TextWatcher {
            private var isUpdating = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return
                isUpdating = true

                val input = s.toString()
                val formattedInput = formatUserInput(input)

                if (formattedInput != input) {
                    holder.amountInput.setText(formattedInput)
                    holder.amountInput.setSelection(formattedInput.length)
                }

                if (holder.amountInput.hasFocus()) {
                    val amount = parseAmount(formattedInput)
                    onAmountChanged(currency.name, amount)
                }

                isUpdating = false
            }
        }

        holder.amountInput.addTextChangedListener(watcher)
        holder.amountInput.setTag(R.id.currencyAmountWatcher, watcher)

        // Устанавливаем курсор в конец при получении фокуса
        holder.amountInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                holder.amountInput.setSelection(holder.amountInput.text.length)
            }
        }
    }

    private fun formatAmountForDisplay(amount: Double): String {
        return if (amount == 0.0) "" else decimalFormat.format(amount)
    }

    private fun parseAmount(input: String): Double {
        return input.toDoubleOrNull() ?: 0.0
    }

    private fun formatUserInput(input: String): String {
        val cleaned = input.replace(",", ".")
        return when {
            cleaned.isEmpty() -> ""
            cleaned == "." -> "0."
            cleaned.startsWith(".") -> "0$cleaned"
            cleaned.count { it == '.' } > 1 -> {
                val parts = cleaned.split(".")
                parts[0] + "." + parts.subList(1, parts.size).joinToString("")
            }
            else -> cleaned
        }
    }

    override fun getItemCount(): Int = currencies.size

    fun updateCurrencies(inputCurrency: String, inputAmount: Double) {
        val inputRate = currencies.find { it.name == inputCurrency }?.rate ?: return

        for (currency in currencies) {
            val convertedAmount = if (currency.name != inputCurrency)
                inputAmount / inputRate * currency.rate
            else
                inputAmount

            // Обновляем только если значение действительно изменилось
            if (currency.convertedValue != convertedAmount) {
                currency.convertedValue = convertedAmount
                notifyItemChanged(currencies.indexOf(currency), currency)
            }
        }
    }

    fun moveCurrency(fromPosition: Int, toPosition: Int) {
        val fromCurrency = currencies.removeAt(fromPosition)
        currencies.add(toPosition, fromCurrency)
        notifyItemMoved(fromPosition, toPosition)
    }

    fun removeCurrency(position: Int) {
        currencies.removeAt(position)
        notifyItemRemoved(position)
    }

@SuppressLint("DiscouragedApi")
private fun setFlag(imageView: ImageView, currencyCode: String) {
    val countryCode = currencyCode.lowercase().take(2)
    var resourceId = imageView.context.resources.getIdentifier(
        countryCode,
        "drawable",
        imageView.context.packageName
    )

    if (resourceId == 0) {
        // Устанавливаем флаг США по умолчанию, если флаг не найден
        resourceId = imageView.context.resources.getIdentifier(
            "us",
            "drawable",
            imageView.context.packageName
        )
    }
    imageView.setImageResource(resourceId)
}
    override fun onBindViewHolder(holder: CurrencyViewHolder, position: Int, payloads: List<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            val currency = payloads[0] as Currency
            val formattedValue = formatAmountForDisplay(currency.convertedValue)
            if (holder.amountInput.text.toString() != formattedValue) {
                holder.amountInput.setText(formattedValue)
            }
        }
    }
}
package com.example.currencyconverterv2.models

data class Currency(
    val name: String,
    val rate: Double,
    var convertedValue: Double = 0.0
)

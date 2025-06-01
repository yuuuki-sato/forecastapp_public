package com.example.forecastapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
class WeatherViewModel : ViewModel() {
    private val jma = JMA()

    private val _dataList = MutableStateFlow<List<WeatherDataElement>?>(null)
    val dataList: StateFlow<List<WeatherDataElement>?> = _dataList    //外部から読み取りのみ可能

    /**
     * 気象庁から天気情報を取得する
     */
    fun fetchWeatherData(region: String) {
        viewModelScope.launch {
            val data: List<WeatherDataElement>? = jma.getJson(region)
            if (!data.isNullOrEmpty()) {
                _dataList.value = data
            }
        }
    }
}
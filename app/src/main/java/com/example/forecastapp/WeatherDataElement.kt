package com.example.forecastapp

/**
 * オンラインツールでJsonからdata class WeatherDataElementを作成した
 * online tool:  https://app.quicktype.io/
 */
data class WeatherDataElement (
    val publishingOffice: String,
    val reportDatetime: String,
    val timeSeries: List<TimeSery>,
    val tempAverage: PAverage? = null,
    val precipAverage: PAverage? = null
)

data class PAverage (
    val areas: List<PrecipAverageArea>
)

data class PrecipAverageArea (
    val area: AreaArea,
    val min: String,
    val max: String
)

data class AreaArea (
    val name: String,
    val code: String
)

data class TimeSery (
    val timeDefines: List<String>,
    val areas: List<TimeSeryArea>
)

data class TimeSeryArea (
    val area: AreaArea,
    val weatherCodes: List<String>? = null,
    val weathers: List<String>? = null,
    val winds: List<String>? = null,
    val waves: List<String>? = null,
    val pops: List<String>? = null,
    val temps: List<String>? = null,
    val reliabilities: List<String>? = null,
    val tempsMin: List<String>? = null,
    val tempsMinUpper: List<String>? = null,
    val tempsMinLower: List<String>? = null,
    val tempsMax: List<String>? = null,
    val tempsMaxUpper: List<String>? = null,
    val tempsMaxLower: List<String>? = null
)

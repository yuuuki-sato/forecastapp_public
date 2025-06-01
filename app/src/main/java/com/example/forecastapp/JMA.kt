package com.example.forecastapp

import java.net.HttpURLConnection
import java.net.URL
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.reflect.Type

class JMA {
    /**
     * 気象庁からJSONを取得する
     * - https://toronavi.com/connection-httpurlconnection
     * - https://engineering.konso.me/articles/kotlin-httpconnection/
     */
    suspend fun getJson(prefName: String): List<WeatherDataElement>? {
        return withContext(Dispatchers.IO) {
            // URL設定
            val code: Int = Prefectures.maps[prefName] ?: return@withContext null
            val url =
                URL("https://www.jma.go.jp/bosai/forecast/data/forecast/${"%06d".format(code)}.json")

            val con = url.openConnection() as HttpURLConnection
            val str: String
            try {
                // 接続設定
                con.connectTimeout = 5000  // ms
                con.readTimeout = 5000 // ms
                con.requestMethod = "GET"

                // 接続を確立
                con.connect()

                // レスポンスを取得
                str = con.inputStream.bufferedReader(Charsets.UTF_8).use { br ->
                    br.readLines().joinToString("")
                }

                con.disconnect()
            } catch (exception: Exception) {
                con.disconnect()
                return@withContext null
            }
            return@withContext makeList(str)
        }
    }

    /**
     * JSONからリストを作成する
     * serialization by GSON
     */
    private fun makeList(json: String): List<WeatherDataElement> {
        val list: List<WeatherDataElement>
        val gson = Gson()
        val listType: Type = com.google.gson.reflect.TypeToken.getParameterized(
            ArrayList::class.java, WeatherDataElement::class.java).type
        list = gson.fromJson(json, listType)

        return list
    }
}

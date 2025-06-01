package com.example.forecastapp

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.forecastapp.ui.theme.ForecastAppTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class MainActivity : ComponentActivity() {
    private val viewModel: WeatherViewModel = WeatherViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ForecastAppTheme {
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    App(viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(viewModel: WeatherViewModel) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    var region by rememberSaveable { mutableStateOf("") }
    val dataList by viewModel.dataList.collectAsState()

    // Jsonを取得し、dataに格納
    LaunchedEffect(key1 = region) {
        viewModel.fetchWeatherData(region)
    }

    //Appの中身
    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .fillMaxWidth(),
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                title = {
                    Text(
                        "天気",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold
                    )
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 表示する地域を選択
            DropdownMenuBox(onRegionChange = { newRegion -> region = newRegion })

            LazyColumn {
                // 気象台と発表日時を表示
                item {
                    dataList?.get(0)?.let { ShowGeneralInfo(it) }
                }

                // 1日分の天気予報を表示
                items(dataList?.get(0)?.timeSeries?.get(0)?.areas?.size ?: 0) {
                        i -> dataList?.get(0)?.let { ShowDaily(it, i) }
                }

                // 週間天気予報を表示
                items(dataList?.get(1)?.timeSeries?.get(0)?.areas?.size ?: 0) {
                        i -> dataList?.get(1)?.let { ShowWeekly(it, i) }
                }
            }
        }
    }
}

/**
 * 週間天気予報がクリックされた時にインデックスを表示する
 */
@Composable
fun ShowIndexOnWeeklyClick(){
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // 空白
            Text(text = " ")

            // 降水確率
            Text(
                textAlign = TextAlign.Center,
                text = "降水確率"
            )

            //　最低気温
            Text(
                textAlign = TextAlign.Center,
                text = "最低気温"
            )

            // 最高気温
            Text(
                textAlign = TextAlign.Center,
                text = "最高気温"
            )
        }
    }
}

/**
 * 週間天気予報がクリックされた時に詳細を表示する
 */
@Composable
fun ShowDetailOnWeeklyClick(data: WeatherDataElement, day: Int, area: Int){
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // 月日
            Text(
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                text = formatDate(data.timeSeries[0].timeDefines[day], 1)
            )

            // 降水確率
            Text(
                text = when {
                    data.timeSeries[0].areas[area].pops?.get(day).isNullOrEmpty() -> "-"
                    else -> data.timeSeries[0].areas[area].pops?.get(day) + "%"
                }
            )

            //　最低気温
            Text(
                text = when {
                    data.timeSeries[1].areas[area].tempsMin?.get(day).isNullOrEmpty() -> "-"
                    else -> data.timeSeries[1].areas[area].tempsMin?.get(day) + "℃"
                }
            )

            // 最高気温
            Text(
                text = when {
                    data.timeSeries[1].areas[area].tempsMax?.get(day).isNullOrEmpty() -> "-"
                    else -> data.timeSeries[1].areas[area].tempsMax?.get(day) + "℃"
                }
            )
        }
    }
}

/**
 * 週間天気予報を表示する
 */
@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowWeekly(data: WeatherDataElement, area: Int){
    val expanded = mutableStateOf(false)
    val weeklySize: Int = data.timeSeries[0].timeDefines.size

    Card(
        onClick = { expanded.value = !expanded.value },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // 地域
        Text(
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            text = "${data.timeSeries[0].areas[area].area.name}の週間天気予報"
        )

        if(expanded.value){
            LazyRow {
                // インデックスを表示
                item{
                    ShowIndexOnWeeklyClick()
                }
                // 明々後日からの天気予報を表示
                items(weeklySize - 1) { i ->
                    ShowDetailOnWeeklyClick(data, i + 1, area)
                }
            }
        }
    }
}

/**
 * 日付をフォーマットする
 */
@Composable
fun formatDate(dateString: String, i: Int): String {
    val formatPattern = when(i) {
        0 -> "yyyy年MM月dd日HH時 発表"
        1 -> "M/d"
        else -> return ""
    }

    return try {
        LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME)
            .format(DateTimeFormatter.ofPattern(formatPattern))
    } catch (e: DateTimeParseException) {
        ""
    }
}

/**
 * 取得した気象台と発表日時を表示する
 */
@Composable
fun ShowGeneralInfo(data: WeatherDataElement) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ){
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 気象台を表示
            Text (
                text = data.publishingOffice,
                modifier = Modifier.padding(4.dp),
                textAlign = TextAlign.Center
            )
            // 発表日時を表示
            Text(
                text = formatDate(data.reportDatetime, 0),
                modifier = Modifier.padding(4.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 2日間の天気予報を表示する
 */
@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowDaily(data: WeatherDataElement, area: Int) {
    val expanded = mutableStateOf(false)

    Card(
        onClick = { expanded.value = !expanded.value },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(horizontal = 4.dp, vertical = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                val someWeathers = data.timeSeries[0].areas.getOrNull(area)
                Text(
                    fontWeight = FontWeight.Bold,
                    text = someWeathers?.area?.name ?: " - "
                )

                Text(
                    "今日：" + (someWeathers?.weathers?.getOrNull(0) ?: " - ")
                )

                if (expanded.value) {
                    Text(" ")
                    ShowDetailOnDailyClick(data, area, 0)
                    Text(" ")
                    Text("明日：" + (someWeathers?.weathers?.getOrNull(1) ?: " - "))
                    ShowDetailOnDailyClick(data, area, 1)
                    Text(" ")
                    Text("明後日：" + (someWeathers?.weathers?.getOrNull(2) ?: " - "))
                }
            }
        }
    }
}

/**
 * 2日間の天気予報がクリックされたときに詳細を表示する
 */
@Composable
fun ShowDetailOnDailyClick(data: WeatherDataElement, area: Int, day: Int) {
    // 降水確率
    Text(
        "降水確率",
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )
    // index
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        for (i in 0..3) {
            Box(
                modifier = Modifier
                    .weight(1F)
                    .border(width = 1.dp, color = Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${i*6}-${i*6+6}",
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    val pops = data.timeSeries[1].areas.getOrNull(area)?.pops
    var pop: Array<String> = arrayOf(" - ", " - ", " - ", " - ")
    when(pops?.size?.rem(4)){
        1 -> {
            if(day == 0) {
                pop = arrayOf(" - ", " - ", " - ", pops[0])
            } else if(day == 1) {
                pop = arrayOf(pops[1], pops[2], pops[3], pops[4])
            }
        }
        2 -> {
            if(day == 0) {
                pop = arrayOf(" - ", " - ", pops[0], pops[1])
            } else if(day == 1) {
                pop = arrayOf(pops[2], pops[3], pops[4], pops[5])
            }
        }
        3 -> {
            if(day == 0) {
                pop = arrayOf(" - ", pops[0], pops[1], pops[2])
            } else if(day == 1) {
                pop = arrayOf(pops[3], pops[4], pops[5], pops[6])
            }
        }
        0 -> {
            if(day == 0) {
                pop = arrayOf(pops[0], pops[1], pops[2], pops[3])
            } else if(day == 1) {
                pop = arrayOf(pops[4], pops[5], pops[6], pops[7])
            }
        }
    }

    // 6時間ごとの降水確率
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        pop.forEach { item ->
            Box(
                modifier = Modifier
                    .weight(1F)
                    .border(width = 1.dp, color = Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${item}%",
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    // 気温
    val temps = data.timeSeries[2].areas.getOrNull(area)?.temps
    val minTemp: String
    val maxTemp: String
    // ここの仕様が分からへんから、なんとなく推測して書いた
    when(temps?.size){
        1 -> {
            minTemp = " - "
            maxTemp = if(day == 1){ temps[0] } else{ " - " }
        }
        2 -> {
            minTemp = if(day == 1){ temps[0] } else{ " - " }
            maxTemp = if(day == 0){ " - "} else{ temps[1] }
        }
        3 -> {
            minTemp = if(day == 1){ temps[0] } else{ " - " }
            maxTemp = temps[day * 2]
        }
        4 -> {
            minTemp = if(day == 0){ " - " } else{ temps[day * 2] }
            maxTemp = temps[day * 2 + 1]
        }
        else -> {
            minTemp = " - "
            maxTemp = " - "
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            "最低気温：${minTemp}℃, 最高気温：${maxTemp}℃",
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 都市を選択するドロップダウンメニュー
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenuBox(onRegionChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf("都市を選択") }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {expanded = !expanded }
        ) {
            TextField(
                value = selectedText,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                Prefectures.maps.keys.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(text = item) },
                        onClick = {
                            selectedText = item
                            expanded = false
                            onRegionChange(selectedText)
                        }
                    )
                }
            }
        }
    }
}

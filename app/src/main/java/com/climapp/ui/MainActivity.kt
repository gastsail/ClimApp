package com.climapp.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.climapp.R
import com.climapp.model.Clima
import com.climapp.ui.adapter.DailyWeatherListAdapter
import com.climapp.utils.ClimappUtils
import com.dezlum.codelabs.getjson.GetJson
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import android.app.SearchManager
import android.content.Context
import android.database.MatrixCursor
import android.location.Address
import android.provider.BaseColumns
import android.widget.CursorAdapter
import android.widget.SimpleCursorAdapter


class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val geoCoder: Geocoder by lazy { Geocoder(this) }
    private var suggestionList = mutableListOf<List<Address>>()

    companion object {
        private const val locationRequestCode = 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        checkForPermission()
    }

    fun checkForPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                locationRequestCode
            )

        } else {
            getLatitudLongitud()
        }
    }

    fun getLatitudLongitud() {
        fusedLocationProviderClient.lastLocation.addOnSuccessListener(this) { location ->
            if (location != null) {
                val latitud = location.latitude
                val longitud = location.longitude
                updateWeather(latitud, longitud, false)
            }
        }
    }

    private fun updateWeather(latitud: Double, longitud: Double, isCitySearchView: Boolean) {
        var apiUrl =
            "https://api.darksky.net/forecast/6f7e254206a8285a9e7b0506af425ef9/$latitud,$longitud?lang=es&units=si"
        val currentObj = GetJson().AsJSONObject(apiUrl).get("currently")
        val mainDataObj = GetJson().AsJSONObject(apiUrl)
        val dailyArray = mainDataObj.getAsJsonObject("daily").getAsJsonArray("data")
        val dayObj = dailyArray.get(0)

        val gson = Gson()
        val datosClima = gson.fromJson(currentObj, Clima::class.java)
        val dayClima = gson.fromJson(dayObj, Clima::class.java)
        val datosGenerales = gson.fromJson(mainDataObj, Clima::class.java)

        val datosPronosticoExtendido = ArrayList<Clima>()

        for (index in (1..5)) {
            val itemJsonObj = dailyArray.get(index).asJsonObject
            val itemKotlinObj = gson.fromJson(itemJsonObj, Clima::class.java)
            Log.d(
                "A",
                "min: ${itemKotlinObj.temperatureLow}, max: ${itemKotlinObj.temperatureHigh}"
            )

            val dailyWeatherObj = Clima()

            dailyWeatherObj.time = itemKotlinObj.time
            dailyWeatherObj.icon = itemKotlinObj.icon
            dailyWeatherObj.temperatureLow = itemKotlinObj.temperatureLow
            dailyWeatherObj.temperatureHigh = itemKotlinObj.temperatureHigh

            datosPronosticoExtendido.add(dailyWeatherObj)
        }

        val date = SimpleDateFormat("dd/MM/yyyy").format(Date(dayClima.time * 1000))

        tvCurrentDayName.text = ClimappUtils.getDayOfWeek(date)
        tvCurrentCity.text = getCity(datosGenerales.timezone);
        tvCurrentWeatherTemperature.text = "${ClimappUtils.getRounded(datosClima.temperature)} ºC"
        Glide.with(this).load("${ClimappUtils.imagesUrl}${datosClima.icon}.png").centerCrop()
            .into(imgViewCurrentWeatherIcon)
        tvMaxAndMin.text =
            "max: ${ClimappUtils.getRounded(dayClima.temperatureHigh)} ºC  min: ${ClimappUtils.getRounded(
                dayClima.temperatureLow
            )} ºC"
        tvCurrentWeatherSummary.text = datosClima.summary

        fillRecyclerViewExtendedForecast(datosPronosticoExtendido)

        progressBar.visibility = View.GONE
        relativeClima.visibility = View.VISIBLE
        relativeRecycler.visibility = View.VISIBLE
        tvDatosClima.visibility = View.GONE

        if (isCitySearchView) {
            tvHumedadProbLluvia.visibility = View.VISIBLE
            tvHumedadProbLluvia.text =
                "humedad: ${ClimappUtils.getRounded(dayClima.humidity) * 100}% prob. lluvia: ${ClimappUtils.getRounded(
                    dayClima.precipProbability
                )}%"
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)

        val searchView = menu?.findItem(R.id.action_search_city_weather)?.actionView as SearchView
        searchView.queryHint = "Escriba la ciudad a buscar"
        val columNames = arrayOf(SearchManager.SUGGEST_COLUMN_TEXT_1)
        val viewIds = intArrayOf(android.R.id.text1)
        val adapter: CursorAdapter = SimpleCursorAdapter(
            this,
            android.R.layout.simple_list_item_1, null, columNames, viewIds)
         searchView.suggestionsAdapter = adapter


        searchView.setOnSuggestionListener(object : SearchView.OnSuggestionListener {
            override fun onSuggestionSelect(position: Int): Boolean {
                return false
            }

            override fun onSuggestionClick(position: Int): Boolean {
                val countryName = suggestionList[position][position].countryName
                searchView.setQuery(countryName, true)
                searchView.clearFocus()
                return true
            }
        })


        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {

                val addresses = geoCoder.getFromLocationName(query, 5)
                if (addresses.size > 0) {
                    suggestionList.add(addresses)
                    val latitud = addresses[0].latitude
                    val longitud = addresses[0].longitude
                    updateWeather(latitud, longitud, true)
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "No se encontro la ciudad solicitada",
                        Toast.LENGTH_SHORT
                    ).show()
                }


                val columns = arrayOf(
                    BaseColumns._ID,
                    SearchManager.SUGGEST_COLUMN_TEXT_1,
                    SearchManager.SUGGEST_COLUMN_INTENT_DATA
                )
                val matrixCursor = MatrixCursor(columns)
                for ((index, value) in suggestionList.withIndex()) {
                    val tmp = arrayOf(index, value, value)
                    matrixCursor.addRow(tmp)
                }
                adapter.swapCursor(matrixCursor)
                adapter.notifyDataSetChanged()

                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                return false
            }
        })

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_weather_current_city -> {
                getLatitudLongitud()
            }
        }

        return super.onOptionsItemSelected(item);
    }

    fun fillRecyclerViewExtendedForecast(datosPronosticoExtendido: List<Clima>) {
        rvExtendedForecast.setHasFixedSize(true)
        rvExtendedForecast.layoutManager = LinearLayoutManager(this)
        val mListAdapter = DailyWeatherListAdapter(this, datosPronosticoExtendido)
        rvExtendedForecast.adapter = mListAdapter
    }

    fun getCity(timezone: String): String {
        val timezoneList = timezone.split("/")
        return timezoneList[timezoneList.size - 1]
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1000 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLatitudLongitud()
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

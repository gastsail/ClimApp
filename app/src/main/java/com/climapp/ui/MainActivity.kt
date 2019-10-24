package com.climapp.ui

import android.Manifest
import android.app.SearchManager
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.BaseAdapter
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

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val geoCoder:Geocoder by lazy { Geocoder(this) }

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

    fun checkForPermission(){
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
                updateWeather(latitud,longitud)
            }
        }
    }

    private fun updateWeather(latitud:Double,longitud:Double) {
        var apiUrl = "https://api.darksky.net/forecast/6f7e254206a8285a9e7b0506af425ef9/$latitud,$longitud?lang=es&units=si"
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
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.menu,menu)
        val searchView= menu?.findItem(R.id.action_search)?.actionView as SearchView
        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{

            override fun onQueryTextSubmit(query: String?): Boolean {
                val addresses = geoCoder.getFromLocationName(query,5)
                if(addresses.size > 0){
                    val latitud = addresses[0].latitude
                    val longitud = addresses[0].longitude
                    updateWeather(latitud,longitud)
                }else{
                    Toast.makeText(this@MainActivity,"No se encontro la ciudad solicitada",Toast.LENGTH_SHORT).show()
                }

                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        return super.onCreateOptionsMenu(menu)

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

    fun roundToDecimals(number: Double, numDecimalPlaces: Int): Double {
        val factor = Math.pow(10.0, numDecimalPlaces.toDouble())
        return Math.round(number * factor) / factor
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

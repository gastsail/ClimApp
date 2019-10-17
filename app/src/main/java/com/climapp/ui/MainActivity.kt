package com.climapp.ui

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.climapp.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import com.dezlum.codelabs.getjson.GetJson
import com.climapp.model.Clima
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var latitud: Double = -31.43294
    private var longitud: Double = -64.2768856
    var apiUrl = "https://api.darksky.net/forecast/6f7e254206a8285a9e7b0506af425ef9/$latitud,$longitud?lang=es"


    companion object {
        private const val locationRequestCode = 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
                latitud = location.latitude
                longitud = location.longitude

                val currentObj = GetJson().AsJSONObject(apiUrl).get("currently")
                val mainDataObj = GetJson().AsJSONObject(apiUrl)
                val dayObj = mainDataObj.getAsJsonObject("daily").getAsJsonArray("data").get(0)

                val gson = Gson()
                val datosClima = gson.fromJson(currentObj,Clima::class.java)
                val dayClima = gson.fromJson(dayObj,Clima::class.java)
                val datosGenerales = gson.fromJson(mainDataObj,Clima::class.java)
                val temperaturaEnGrados = (datosClima.temperature - 32) * 5/9

                val date = SimpleDateFormat("dd/MM/yyyy").format(Date(dayClima.time*1000))

                tvCurrentDayName.text = getDayOfWeek(date)
                tvCurrentCity.text = datosGenerales.timezone
                tvCurrentWeatherTemperature.text = roundToDecimals(temperaturaEnGrados,2).toString()+" ºC"
                tvCurrentWeatherSummary.text = datosClima.summary
                tvMaxAndMin.text = "max: ${(roundToDecimals((dayClima.temperatureHigh - 32) * 5/9,2))} ºC  min: ${(roundToDecimals((dayClima.temperatureLow - 32) * 5/9,2))} ºC"

                progressBar.visibility = View.GONE
                relativeClima.visibility = View.VISIBLE
                relativeRecycler.visibility = View.VISIBLE
                tvDatosClima.visibility = View.GONE

            }
        }
    }

    fun getDayOfWeek(date: String): String {
        val locale = Locale("es", "ES")
        return LocalDate.parse(date, DateTimeFormatter.ofPattern("dd/MM/yyyy")).getDayOfWeek().getDisplayName(
            TextStyle.FULL,locale)
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

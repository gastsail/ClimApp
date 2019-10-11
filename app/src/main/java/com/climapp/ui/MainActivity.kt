package com.climapp.ui

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.climapp.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import com.dezlum.codelabs.getjson.GetJson
import com.climapp.model.Clima
import com.dezlum.codelabs.getjson.GetJson.convertInputStreamToString
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var latitud: Double = -31.43294
    private var longitud: Double = -64.2768856
    var apiUrl = "https://api.darksky.net/forecast/6f7e254206a8285a9e7b0506af425ef9/$latitud,$longitud"


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
                txt_test_latlong.text = "Latitud $latitud , Longitud: $longitud"
                val jsonObject = GetJson().AsJSONObject(apiUrl).get("currently")
                val gson = Gson()
                val clima = gson.fromJson(jsonObject,Clima::class.java)
                val temperaturaEnGrados = (clima.temperature - 32) * 5/9
                txt_test_latlong.text = "La temperatura actual es de ${roundToDecimals(temperaturaEnGrados,2)} ºC y esta ${clima.summary}"
            }
        }
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

package com.climapp.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.climapp.R
import com.climapp.model.Clima
import com.climapp.utils.ClimappUtils
import java.text.SimpleDateFormat
import java.util.*

class DailyWeatherListAdapter(val context: Context, val dailyWeatherList: List<Clima>) : RecyclerView.Adapter<DailyWeatherListAdapter.ViewHolder>() {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dailyWeatherList.get(position)
        holder.bind(context, item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(
            layoutInflater.inflate(
                R.layout.extended_forecast_list_item,
                parent,
                false
            )
        )
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int {
        return if (dailyWeatherList.isNotEmpty()) {
            dailyWeatherList.size
        } else {
            0
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgViewdailyWeatherIcon = view.findViewById<ImageView>(R.id.imgViewExtendedForecastWeatherIcon)
        val tvDailyWeatherDayName = view.findViewById<TextView>(R.id.tvExtendedForecastDayName)
        val tvDailyWeatherMinTemp = view.findViewById<TextView>(R.id.tvExtendedForecastMinTemperature)
        val tvDailyWeatherMaxTemp = view.findViewById<TextView>(R.id.tvExtendedForecastMaxTemperature)

        fun bind(context: Context, dailyWeatherObj:Clima){
            Glide.with(context).load("${ClimappUtils.imagesUrl}${dailyWeatherObj.icon}.png").centerCrop().into(imgViewdailyWeatherIcon)

            val date = SimpleDateFormat("dd/MM/yyyy").format(Date(dailyWeatherObj.time*1000))
            tvDailyWeatherDayName.text = ClimappUtils.getDayOfWeek(date)

            tvDailyWeatherMinTemp.text = "${ClimappUtils.getRounded(dailyWeatherObj.temperatureLow)} ºC"

            tvDailyWeatherMaxTemp.text = "${ClimappUtils.getRounded(dailyWeatherObj.temperatureHigh)} ºC"
        }
    }

}
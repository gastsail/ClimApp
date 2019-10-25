package com.climapp.model

/**
 * Created by Gastón Saillén on 08 October 2019
 */
data class Clima(var time:Long = 0,var summary:String = "DEFAULT", var icon:String = "DEFAULT",var temperature:Double = 0.0,var timezone:String = "",
                 var temperatureHigh:Double = 0.0,var temperatureLow:Double = 0.0,val humidity: Double = 0.0, val precipProbability: Double = 0.0)
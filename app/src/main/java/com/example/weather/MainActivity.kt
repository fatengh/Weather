package com.example.weather

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.lang.Exception
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private var city = "10001"
    private val API = "8a316bae40ca552c86771c6d73150592"
    private lateinit var etCode: EditText
    private lateinit var btnCode: Button
    private lateinit var btnerror: Button
    private lateinit var rlZip: RelativeLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnerror = findViewById(R.id.btnError)
        rlZip = findViewById(R.id.rlZip)
        etCode = findViewById(R.id.etZip)
        btnCode = findViewById(R.id.btZip)

        btnerror.setOnClickListener {
            city = "10001"
            requestAPI()
        }

        btnCode.setOnClickListener {
            city = etCode.text.toString();
            requestAPI()
            etCode.text.clear()
            val mm = ContextCompat.getSystemService(this, InputMethodManager::class.java)
            mm?.hideSoftInputFromWindow(this.currentFocus?.windowToken, 0)
            rlZip.isVisible = false
        }
        requestAPI()
    }

    private fun requestAPI() {

        println("CITY: $city")
        CoroutineScope(IO).launch {
            updateStatus(-1) //  error nothing enter or wrong value
            val data = async {
                fechtData()// get data
            }.await()
            if (data.isNotEmpty()) {
                updateWeatherData(data) // arrange
                updateStatus(0)
            } else {
                updateStatus(1) // all good
            }
        }
    }

    private suspend fun updateWeatherData(data: String) {
        withContext(Main) {


            val jObj = JSONObject(data)
            val main = jObj.getJSONObject("main")
            val sys = jObj.getJSONObject("sys")
            val wind = jObj.getJSONObject("wind")
            val weather = jObj.getJSONArray("weather").getJSONObject(0)
            val currentTemperature = main.getString("temp")
            val temp = try {
                currentTemperature.substring(0, currentTemperature.indexOf(".")) + "°C"
            } catch (e: Exception) {
                currentTemperature + "°C"
            }
            val minTemperature = main.getString("temp_min")
            val tempMin = "Low: " + minTemperature.substring(0, minTemperature.indexOf(".")) + "°C"
            val maxTemperature = main.getString("temp_max")
            val tempMax = "High: " + maxTemperature.substring(0, maxTemperature.indexOf(".")) + "°C"
            val pressure = main.getString("pressure")
            val humidity = main.getString("humidity")
            val sunrise: Long = sys.getLong("sunrise")
            val sunset: Long = sys.getLong("sunset")
            val windSpeed = wind.getString("speed")
            val weatherDescription = weather.getString("description")
            val address = jObj.getString("name") + ", " + sys.getString("country")



            var add = findViewById<TextView>(R.id.tvAddress)
            var tvlastupdate = findViewById<TextView>(R.id.tvLastUpdated)
            var tvadd = findViewById<TextView>(R.id.tvAddress)
            var tvstate = findViewById<TextView>(R.id.tvStatus)
            var tvtemp = findViewById<TextView>(R.id.tvTemperature)
            var tvtempmin = findViewById<TextView>(R.id.tvTempMin)
            var tvtempmax = findViewById<TextView>(R.id.tvTempMax)
            var tvsunrise = findViewById<TextView>(R.id.tvSunrise)
            var tvsunset = findViewById<TextView>(R.id.tvSunset)
            var tvwind = findViewById<TextView>(R.id.tvWind)
            var tvpress = findViewById<TextView>(R.id.tvPressure)
            var tvhum = findViewById<TextView>(R.id.tvHumidity)
            var llrefresh = findViewById<LinearLayout>(R.id.llRefresh)



            add.text = address
            tvadd.setOnClickListener {
                rlZip.isVisible = true
            }
            tvlastupdate.text = lastdate(jObj)
            tvstate.text = weatherDescription.capitalize(Locale.getDefault())
            tvtemp.text = temp
            tvtempmin.text = tempMin
            tvtempmax.text = tempMax
            tvsunrise.text = SimpleDateFormat(
                "hh:mm a",
                Locale.ENGLISH
            ).format(Date(sunrise * 1000))
            tvsunset.text = SimpleDateFormat(
                "hh:mm a",
                Locale.ENGLISH
            ).format(Date(sunset * 1000))
            tvwind.text = windSpeed
            tvpress.text = pressure
            tvhum.text = humidity
            llrefresh.setOnClickListener { requestAPI() }
        }
    }


    private fun lastdate(jObj: JSONObject): String {

        val lastUpdate: Long = jObj.getLong("dt") //get daate
        val lastUpdateText = "Updated at: " + SimpleDateFormat(
            "dd/MM/yyyy hh:mm a",
            Locale.ENGLISH
        ).format(Date(lastUpdate * 1000))
        return lastUpdateText
    }

    private fun fechtData(): String {
        var data = ""  // get data from url
        try {
            data =
                URL("https://api.openweathermap.org/data/2.5/weather?zip=$city&units=metric&appid=$API")
                    .readText(Charsets.UTF_8)
        } catch (e: Exception) {
            println("Error: $e")
        }
        return data
    }

    private suspend fun updateStatus(state: Int) {
        //states: -1 = loading, 0 = loaded, 1 = error
        withContext(Main) {
            when { // witch view will present
                state == -1 -> {
                    findViewById<ProgressBar>(R.id.pbProgress).visibility = View.VISIBLE
                    findViewById<RelativeLayout>(R.id.rlMain).visibility = View.GONE
                    findViewById<LinearLayout>(R.id.llErrorContainer).visibility = View.GONE
                }
                state == 0 -> {
                    findViewById<ProgressBar>(R.id.pbProgress).visibility = View.GONE
                    findViewById<RelativeLayout>(R.id.rlMain).visibility = View.VISIBLE
                }
                state == 1 -> {
                    findViewById<ProgressBar>(R.id.pbProgress).visibility = View.GONE
                    findViewById<LinearLayout>(R.id.llErrorContainer).visibility = View.VISIBLE
                }
            }
        }
    }
}
package fr.ramond.assignement003

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileInputStream
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.math.*

class ResultDisplay : AppCompatActivity() {

    lateinit var RETURN : Button
    private lateinit var currentFile : String
    private lateinit var averageSpeed : TextView
    private lateinit var totalDistance : TextView
    private lateinit var timeTaken : TextView
    private lateinit var minimumAlt : TextView
    private lateinit var maximumAlt : TextView
    private lateinit var Graph : graph
    private val trackSpeed : MutableList<Double> = mutableListOf<Double>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result_display)

        RETURN = findViewById(R.id.ButtonReturn)

        averageSpeed = findViewById(R.id.AverageSpeed)
        totalDistance = findViewById(R.id.TotalDistance)
        timeTaken = findViewById(R.id.Timetaken)
        minimumAlt = findViewById(R.id.MinimumAlt)
        maximumAlt = findViewById(R.id.MaximumAlt)
        Graph = findViewById(R.id.cv_line_graph)
        currentFile = intent.getStringExtra("_file")!!
        val time = intent.getFloatExtra("_time", 0.0F)

        timeTaken.text = java.lang.String.format(resources.getString(R.string.Time_taken), (time/1000))

        val file = File(Environment.getExternalStorageDirectory().absolutePath + "/GPStracks/" + currentFile)
        //val inputStream = FileInputStream(file)
        val track = readFile(file)

        val distance = processDistance(track)
        //val distanceFormat = BigDecimal(distance).setScale(3, RoundingMode.HALF_EVEN)

        totalDistance.text = java.lang.String.format(resources.getString(R.string.Distance), distance)

        val aveSpeed = processSpeed(track)
        //val aveSpeedFormat = BigDecimal(aveSpeed).setScale(3, RoundingMode.HALF_EVEN)

        averageSpeed.text = java.lang.String.format(resources.getString(R.string.Speed), aveSpeed)

        val min = minAlt(track)
        val max = maxAlt(track)

        minimumAlt.text = java.lang.String.format(resources.getString(R.string.minAlt), min)
        maximumAlt.text = java.lang.String.format(resources.getString(R.string.maxAlt), max)

        Graph.mPoint = trackSpeed
    }

    fun onClickReturn(view : View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    @SuppressLint("SimpleDateFormat")
    private fun readFile(file :File): MutableList<Location> {

        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()

        val fis = FileInputStream(file)
        val dom = builder.parse(fis)
        val root = dom.documentElement
        val items = root.getElementsByTagName("trkpt")

        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")

        val track : MutableList<Location> = mutableListOf<Location>()

        for(i in 0 until items.length){
            val item = items.item(i)
            val attrs = item.attributes
            val props = item.childNodes

            val pt = Location("Point $i")

            pt.latitude = attrs.getNamedItem("lat").textContent.toDouble()
            pt.longitude = attrs.getNamedItem("lon").textContent.toDouble()

            for(j in 0 until props.length){
                val item2 = props.item(j)
                val name = item2.nodeName
                if(name.equals("time")){
                    pt.time = df.parse(item2.firstChild.nodeValue).time
                }
            }

            for(k in 0 until props.length){
                val item3 = props.item(k)
                val name = item3.nodeName
                if(name.equals("ele")){
                    pt.altitude = item3.firstChild.nodeValue.toDouble()
                }
            }

            track.add(pt)
        }
        return track
    }

    private fun processDistance(track : MutableList<Location>) : Double{

        var totalDistance = 0.0
        if(track.size > 2){
            for(i in 0 until track.size-1){
                totalDistance += calculateDistance(track[i], track[i+1])
            }
        }
        return totalDistance
    }

    private fun processSpeed(track : MutableList<Location>) : Double{

        var totalVit = 0.0
        var k = 0

        if(track.size > 2){
            for(i in 0 until track.size - 1){
                val dist = calculateDistance(track[i], track[i+1])
                val time = track[i+1].time - track[i].time
                val vit = (dist / time)*1000
                Toast.makeText(this, vit.toString(), Toast.LENGTH_SHORT).show()
                totalVit += vit
                trackSpeed.add(vit)
                k++
            }
        }
        else{
            //Avoid dividing by 0
                totalVit = -1.0
            k = 1
        }
        return (totalVit/k)
    }

    private fun calculateDistance(p1 : Location, p2 : Location) : Double{

        var lon1 = p1.longitude
        var lat1 = p1.latitude
        var lon2 = p2.longitude
        var lat2 = p2.latitude

        lon1 = Math.toRadians(lon1)
        lat1 = Math.toRadians(lat1)
        lon2 = Math.toRadians(lon2)
        lat2 = Math.toRadians(lat2)

        val dlon = lon2 - lon1
        val dlat = lat2 - lat1
        val a = sin(dlat / 2).pow(2) + cos(lat1) * cos(lat2) *
                sin(dlon / 2).pow(2.0)
        val c = 2 * asin(sqrt(a))

        val r = 6371

        return (c*r) * 1000
    }

    private fun minAlt(track: MutableList<Location>): Int {
        var min = maxAlt(track)
        for(i in 0 until track.size){
            if(track[i].altitude < 0){
                min = track[i].altitude.toInt()
            }
        }
        return min
    }

    private fun maxAlt(track: MutableList<Location>): Int {
        var max = 0
        for(i in 0 until track.size){
            if(track[i].altitude > max){
                max = track[i].altitude.toInt()
            }
        }
        return max
    }
}
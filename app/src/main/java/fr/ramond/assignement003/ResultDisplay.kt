package fr.ramond.assignement003

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import android.widget.TextView
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.math.*

//Process and display the result of the recording, or of a opened file
class ResultDisplay : AppCompatActivity() {

    //Global variable
    lateinit var RETURN : Button
    private lateinit var currentFile : String
    private lateinit var averageSpeed : TextView
    private lateinit var totalDistance : TextView
    private lateinit var timeTaken : TextView
    private lateinit var minimumAlt : TextView
    private lateinit var maximumAlt : TextView
    private lateinit var graph : Graph
    private val trackSpeed : MutableList<Double> = mutableListOf<Double>()

    //The starting point of the activity, will initiate the global variable, and call all processing
    //methods.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result_display)

        //Initiate global variable
        RETURN = findViewById(R.id.ButtonReturn)

        averageSpeed = findViewById(R.id.AverageSpeed)
        totalDistance = findViewById(R.id.TotalDistance)
        timeTaken = findViewById(R.id.Timetaken)
        minimumAlt = findViewById(R.id.MinimumAlt)
        maximumAlt = findViewById(R.id.MaximumAlt)
        graph = findViewById(R.id.cv_line_graph)
        //Get data from last activity
        currentFile = intent.getStringExtra("_file")!!
        val time = intent.getFloatExtra("_time", 0.0F)

        //The easier, display the time taken (not available if it is an opened file)
        timeTaken.text = java.lang.String.format(resources.getString(R.string.Time_taken), (time/1000))

        //Read the file from the external storage
        val file = File(Environment.getExternalStorageDirectory().absolutePath + "/GPStracks/" + currentFile)
        val track = readFile(file)

        //Process the total distance
        val distance = processDistance(track)
        //Display the total distance
        totalDistance.text = java.lang.String.format(resources.getString(R.string.Distance), distance)

        //Process the average speed
        val aveSpeed = processSpeed(track)
        //Display the average speed
        averageSpeed.text = java.lang.String.format(resources.getString(R.string.Speed), aveSpeed)

        //Process maximum and minimum altitude
        val min = minAlt(track)
        val max = maxAlt(track)
        //Display min and max altitude
        minimumAlt.text = java.lang.String.format(resources.getString(R.string.minAlt), min)
        maximumAlt.text = java.lang.String.format(resources.getString(R.string.maxAlt), max)
        //Update the graph data, so that the line can be displayed
        graph.mPoint = trackSpeed
    }

    //Simple method used by the only button of this activity to return to the main page
    fun onClickReturn(view : View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    //Method that read the file in the external storage,
    //and return all recorded point in a list of Location
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

        //Fill the list
        for(i in 0 until items.length){
            val item = items.item(i)
            val attrs = item.attributes
            val props = item.childNodes

            val pt = Location("Point $i")

            //Location data
            pt.latitude = attrs.getNamedItem("lat").textContent.toDouble()
            pt.longitude = attrs.getNamedItem("lon").textContent.toDouble()

            //Time data
            for(j in 0 until props.length){
                val item2 = props.item(j)
                val name = item2.nodeName
                if(name.equals("time")){
                    pt.time = df.parse(item2.firstChild.nodeValue).time
                }
            }

            //Altitude data
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

    //Calculate the total distance from the track
    private fun processDistance(track : MutableList<Location>) : Double{

        var totalDistance = 0.0
        if(track.size > 2){
            for(i in 0 until track.size-1){
                totalDistance += calculateDistance(track[i], track[i+1])
            }
        }
        return totalDistance
    }

    //Calculate the average speed. Also create the speed list to be send to the graph custom view
    private fun processSpeed(track : MutableList<Location>) : Double{

        var totalVit = 0.0
        var k = 0

        if(track.size > 2){
            for(i in 0 until track.size - 1){
                val dist = calculateDistance(track[i], track[i+1])
                val time = track[i+1].time - track[i].time
                val vit = (dist / time)*1000
                totalVit += vit
                //Also fill the speed list
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

    //Method that calculate the distance in meter between two location.
    // Use the latitude and longitude of these location
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

    //Calculate the minimum from all recorded location
    private fun minAlt(track: MutableList<Location>): Int {
        var min = maxAlt(track)
        for(i in 0 until track.size){
            if(track[i].altitude < 0){
                min = track[i].altitude.toInt()
            }
        }
        return min
    }

    //Calculate the maximum altitude from all recorded location
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
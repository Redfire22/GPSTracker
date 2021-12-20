package fr.ramond.assignement003

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.Button
import android.widget.Chronometer
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.checkSelfPermission
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    //Global variable
    private lateinit var StartButton: Button
    private lateinit var chrono: Chronometer
    private lateinit var locationManager: LocationManager
    private var _latitude : Double = 0.0
    private var _longitude : Double = 0.0
    private var _altitude : Double = 0.0
    private lateinit var latitudeText : TextView
    private lateinit var longitudeText : TextView
    private lateinit var writer: FileWriter
    private lateinit var currentFile : String

    val df: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")

    private val mInterval: Long = 5000

    var started = false
    var stopped = false

    //OnCreate function, the starting point of the activity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        StartButton = findViewById(R.id.StartButton)
        chrono = findViewById(R.id.simpleChronometer)

        latitudeText = findViewById(R.id.Latitude)
        longitudeText = findViewById(R.id.Longitude)
    }

    //Method used by the xml button "Start/Stop recording", will start the recording, change to "Stop
    //recording" button when clicked and if clicked a seconds time, will end the recording and start
    //the "ResultDisplay Activity
    fun onClickStart(view: View){
        if(started){ //Case if the button was already clicked
            StartButton.text = getString(R.string.start_recording)
            val time = (SystemClock.elapsedRealtime() - chrono.base).toFloat()
            val intent = Intent(this, ResultDisplay::class.java)
            intent.putExtra("_file", currentFile)
            intent.putExtra("_time", time)
            stopped = true
            closeFile()
            startActivity(intent)

        }
        else{ //Case if the button wasn't already clicked
            StartButton.text = getString(R.string.stop_recording)
            chrono.base = SystemClock.elapsedRealtime()
            chrono.start()
            stopped = false
            getLocation()
            started = true
        }
    }

    //Method used by the "Open file" button to open the open file activity, the recording must be
    //stopped for this button to be available
    fun onClickOpen(view : View){
        if(!started){
            val intent = Intent(this, OpenFile::class.java)
            startActivity(intent)
        }
        else{
            Toast.makeText(this, "App is running, finish the current run to open file", Toast.LENGTH_LONG).show()
        }
    }

    //The method that start the periodic recovering of the location, after asking for permission
    //Here the period is 5000 ms. Also initiate the file saving method. Display the latitude and longitude info
    //on the main activity xml. The record of point is permanent while the app is running, the saving in
    //file is not.
    private fun getLocation(){
        //Check for permission
        if((checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) &&
                (checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            requestPermissions(arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }
        //Create saving file
        createSaveFile()
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0f) { p0 ->
            _latitude = p0.latitude
            _longitude = p0.longitude
            _altitude = p0.altitude
            //If the recording is not running, it won't try to write to the file
            if(!stopped){
                addpoint(_longitude, _latitude, _altitude)
            }
            latitudeText.text = java.lang.String.format(resources.getString(R.string.Latitude_info), _latitude)
            longitudeText.text = java.lang.String.format(resources.getString(R.string.Longitude_info), _longitude)
        }
    }

    //Override of the onRequestPermissionsResult method, recover permission data
    //If the permission is granted, it will continue, if not, a message will be displayed
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out
    String>, grantResults: IntArray) {
        // call the super class version of his first
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // check to see what request code we have
        // Request code 1 mean permission for recording location
        if(requestCode == 1) {
            //Permission not granted
            if(grantResults[0] == PackageManager.PERMISSION_DENIED || grantResults[1] == PackageManager.PERMISSION_DENIED) {
                val toast = Toast.makeText(this, "App can't work without location permission", Toast.LENGTH_LONG)
                toast.show()
            } else {
                //Permission granted
                val toast = Toast.makeText(this, "Location permissions granted", Toast.LENGTH_LONG)
                toast.show()
                getLocation() //Continue
            }
        }
        //Request code 2 mean permission for accessing external storage
        else if(requestCode == 2) {
            if(grantResults[0] == PackageManager.PERMISSION_DENIED || grantResults[1] == PackageManager.PERMISSION_DENIED) {
                //Permission not granted
                val toast = Toast.makeText(this, "App can't work without storage permission", Toast.LENGTH_LONG)
                toast.show()
            } else {
                //Permission granted
                val toast = Toast.makeText(this, "Storage permissions granted", Toast.LENGTH_LONG)
                toast.show()
                createSaveFile() //Continue
            }
        }
    }

    //This method check/ask for external storage permission, and then create, open and start writing the gpx file
    //It also check if the directory GPStracks is already created, and create it if it's not the case.
    //This method does not close the file, nor does it write location data in it.
    @SuppressLint("SimpleDateFormat")
    private fun createSaveFile(){
        //Check for external storage permission
        if((checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) &&
                (checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
            requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE), 2)
            return
        }
        //Header for gpx file
        val header = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" " +
                "?><gpx xmlns=\"http://www.topografix.com/GPX/1/1\" creator=\"MapSource 6.15.5\" " +
                "version=\"1.1\" " +
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"  " +
                "xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 " +
                "http://www.topografix.com/GPX/1/1/gpx.xsd\"><trk>\n"
        val name = "<name>$df</name><trkseg>\n";

        //Check if the GPStracks directory exist, if not create it
        val d = File(android.os.Environment.getExternalStorageDirectory().absolutePath + "/GPStracks")
        if(!d.isDirectory){
            d.mkdirs()
        }
        val now = Date()
        val f = File(android.os.Environment.getExternalStorageDirectory().absolutePath + "/GPStracks" + "/" + df.format(now) +".xml")
        currentFile = df.format(now) + ".xml"
        if(checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            writer = FileWriter(f, false)
            writer.append(header)
            writer.append(name)
        }
    }

    //Add location data to the already opened file.
    //Write a string line
    private fun addpoint(longitude : Double, latitude : Double, altitude : Double){
        val now = Date()
        val segement = "<trkpt lat=\"" + longitude + "\" lon=\"" + latitude + "\"><time>" + df.format(now) + "</time><ele>" + altitude + "</ele></trkpt>\n";
        if(writer != null){
            writer.append(segement)
        }
    }

    //Close existing tag and close the file itself
    private fun closeFile(){

        val footer = "</trkseg></trk></gpx>"
        if(writer != null){
            writer.append(footer)
            writer.flush()
            writer.close()
        }
    }



}

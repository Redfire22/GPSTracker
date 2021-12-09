package fr.ramond.assignement003

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.Button
import android.widget.Chronometer

class MainActivity : AppCompatActivity() {

    private lateinit var StartButton: Button
    private lateinit var chrono: Chronometer

    var started = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        StartButton = findViewById(R.id.StartButton)
        chrono = findViewById(R.id.simpleChronometer)
    }

    fun onClickStart(view: View){
        if(started){
            StartButton.text = "Start recording"
            val intent = Intent(this, ResultDisplay::class.java)
            startActivity(intent)

        }
        else{
            StartButton.text = "Stop recording"
            chrono.setBase(SystemClock.elapsedRealtime());
            chrono.start()
            started = true
        }

    }
}
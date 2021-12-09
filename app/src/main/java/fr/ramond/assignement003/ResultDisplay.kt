package fr.ramond.assignement003

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button

class ResultDisplay : AppCompatActivity() {

    lateinit var RETURN : Button;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result_display)

        RETURN = findViewById(R.id.ButtonReturn)
    }

    fun onClickReturn(view : View){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}
package fr.ramond.assignement003

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import java.io.File

class OpenFile : AppCompatActivity() {

    val fileLocation = Environment.getExternalStorageDirectory().absolutePath + "/GPStracks"

    private lateinit var list : ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_file)

        val directory = File(Environment.getExternalStorageDirectory().absolutePath + "/GPStracks")

        val files = directory.listFiles()

        list = findViewById(R.id.List)

        val filesName = mutableListOf<String>()

        for(i  in files.indices){
            filesName.add(files[i].name)
        }

        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, filesName)

        list.adapter = adapter

        list.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = filesName[position]

            val intent = Intent(this, ResultDisplay::class.java)

            intent.putExtra("_file", selectedItem)
            startActivity(intent)
        }

    }
}
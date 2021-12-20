package fr.ramond.assignement003

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import java.io.File

//Basic activity, it will display a list of existing gpx file in the directory
class OpenFile : AppCompatActivity() {

    //Global variable
    private lateinit var list : ListView

    //The only method of this activity, create the listview and fill it with existing file
    //Also create the Listener, in case the user click on one of the file
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_file)

        //The directory
        val directory = File(Environment.getExternalStorageDirectory().absolutePath + "/GPStracks")
        //The list of file in the directory
        val files = directory.listFiles()

        list = findViewById(R.id.List)

        val filesName = mutableListOf<String>()

        for(i  in files.indices){
            filesName.add(files[i].name)
        }

        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, filesName)

        list.adapter = adapter
        //Create the listener
        list.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = filesName[position]

            val intent = Intent(this, ResultDisplay::class.java)

            intent.putExtra("_file", selectedItem)
            startActivity(intent)
        }

    }
}
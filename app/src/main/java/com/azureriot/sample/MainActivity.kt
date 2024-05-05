package com.azureriot.sample

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.azure.storage.blob.BlobClient
import com.azure.storage.blob.BlobContainerClient
import com.azure.storage.blob.BlobServiceClient
import com.azure.storage.blob.BlobServiceClientBuilder
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.json.JSONException
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private var tv: TextView? = null

    private val CONNECTION_STRING =
        "DefaultEndpointsProtocol=https;AccountName=sensor1data;AccountKey=zin++eomthOe501JF2P7VJefVr646GhbuCMbqaMyMfdl59eH6n3fwIbGKzuzbnfyg61aRqE1cjsv+ASt5YbUjQ==;EndpointSuffix=core.windows.net"
    private val CONTAINER_NAME = "onlinesensordata"
    private val BLOB_NAME = "0_161b469607c84a04a037505b8ebeaca3_1.json"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tv = findViewById(R.id.ShowOutput)
        val refreshButton:Button = findViewById(R.id.refreshButton)

        refreshButton.setOnClickListener(View.OnClickListener { v: View? -> fetchData() })
    }

    private fun fetchData() {
        Thread {
            try {
                downloadBlob(CONNECTION_STRING,CONTAINER_NAME, BLOB_NAME
                )
            } catch (e: IOException) {
                showError(e.toString())
            } catch (e: JSONException) {
                showError(e.toString())
            }
        }.start()
    }

    @Throws(IOException::class, JSONException::class)
    private fun downloadBlob(connectionString: String, containerName: String, blobName: String) {
        try {
            val blobServiceClient: BlobServiceClient =
                BlobServiceClientBuilder().connectionString(connectionString).buildClient()
            val blobContainerClient: BlobContainerClient = blobServiceClient.getBlobContainerClient(containerName)
            val blobClient: BlobClient = blobContainerClient.getBlobClient(blobName)
            val downloadFile = File(externalCacheDir, blobName)
            val outputStream: OutputStream = FileOutputStream(downloadFile)

            try {
                blobClient.download(outputStream)
                println("Blob downloaded successfully.")

                val jsonContent: String = parseJson(downloadFile).toString()

                updateUI(jsonContent)
            } catch (e:Exception){
                Log.d("Exception",e.toString())
            }

        }catch (e:LinkageError){
            Log.d("LinkageError",e.toString())

        }
    }

    @Throws(JSONException::class)
    private fun parseJson(file: File) {
        var data: String = ""
        val gson = Gson()

        try {
            BufferedReader(InputStreamReader(FileInputStream(file))).use { reader ->
                val jsonElement: JsonElement = gson.fromJson(reader, JsonElement::class.java)

                if (jsonElement.isJsonArray) {
                    val jsonArray: JsonArray = jsonElement.asJsonArray

                    // Output the parsed data
                    println("Parsed data:")
                    for (i in 0 until jsonArray.size()) {
                        val jsonObject: JsonObject = jsonArray.get(i).asJsonObject
                        val temperature = jsonObject.get("temperature").asDouble
                        val humidity = jsonObject.get("humidity").asDouble

                        println("Temperature: $temperature")
                        println("Humidity: $humidity")

                        // Update UI for each data point
                        updateUI("Temperature: $temperature\nHumidity: $humidity")
                    }
                } else {
                    println("Invalid JSON format")
                }
            }
        } finally {
            Log.d("Status", "Retrieved")
        }
    }

    private fun updateUI(jsonContent: String) {
        val gson = Gson()

        // Deserialize the JSON string into WeatherData object
        val weatherData = gson.fromJson(jsonContent, WeatherData::class.java)

        // Parse timestamp to Date object
//        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
//        val timestamp = sdf.parse(weatherData.timestamp)

        // Access relevant data
        val temperature = weatherData.temperature
        val humidity = weatherData.humidity

        // Print the relevant data
//        println("Timestamp: $timestamp")
        println("Temperature: $temperature")
        println("Humidity: $humidity")
        runOnUiThread { tv!!.text =  "Temperature: "+ temperature + "\n"+ "Humidity: " + humidity }
    }


    private fun showError(errorMessage: String) {
        runOnUiThread { tv!!.text = "Error: $errorMessage" }
    }
}
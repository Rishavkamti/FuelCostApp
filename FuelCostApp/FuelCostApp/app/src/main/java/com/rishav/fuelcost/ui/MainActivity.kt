package com.rishav.fuelcost.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.rishav.fuelcost.R
import com.rishav.fuelcost.util.Prefs

class MainActivity : AppCompatActivity() {

    private lateinit var mileageInput: EditText
    private lateinit var fuelPriceInput: EditText
    private lateinit var apiKeyInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mileageInput = findViewById(R.id.inputMileage)
        fuelPriceInput = findViewById(R.id.inputFuelPrice)
        apiKeyInput = findViewById(R.id.inputApiKey)

        // Pehle se saved values load karo
        mileageInput.setText(Prefs.getMileage(this).toString())
        fuelPriceInput.setText(Prefs.getFuelPrice(this).toString())
        apiKeyInput.setText(Prefs.getMapsApiKey(this))

        findViewById<Button>(R.id.btnSave).setOnClickListener {
            val mileage = mileageInput.text.toString().toFloatOrNull()
            val fuelPrice = fuelPriceInput.text.toString().toFloatOrNull()
            val apiKey = apiKeyInput.text.toString().trim()

            if (mileage == null || fuelPrice == null || mileage <= 0 || fuelPrice <= 0) {
                Toast.makeText(this, "Mileage aur fuel price sahi se bharo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (apiKey.isEmpty()) {
                Toast.makeText(this, "Google Maps API key daalo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Prefs.saveVehicleDetails(this, mileage, fuelPrice, apiKey)
            Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnEnableAccessibility).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            Toast.makeText(
                this,
                "'Fuel Cost Helper' service ko dhundo aur ON karo",
                Toast.LENGTH_LONG
            ).show()
        }

        findViewById<Button>(R.id.btnEnableOverlay).setOnClickListener {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }
    }
}

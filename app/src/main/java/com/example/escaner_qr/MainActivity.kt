/*
package com.example.escaner_qr

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}*/

package com.example.escaner_qr

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanQRCode
import android.widget.Button
import android.widget.TextView
import android.widget.ImageView


class MainActivity : AppCompatActivity() {

    // Registra el lanzador para el escáner
    private val scanQrCodeLauncher = registerForActivityResult(ScanQRCode()) { result ->
        when (result) {
            is QRResult.QRSuccess -> {
                val scannedText = result.content.rawValue
                tvResult.text = "$scannedText"
            }
            QRResult.QRUserCanceled -> {
                tvResult.text = "Escaneo cancelado por el usuario"
            }
            QRResult.QRMissingPermission -> {
                tvResult.text = "Permiso de cámara no concedido"
            }
            is QRResult.QRError -> {
                tvResult.text = "Error: ${result.exception.message}"
            }
        }
    }

    // Declara las vistas
    private lateinit var btnScan: ImageView
    private lateinit var tvResult: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Configura el listener para los insets de la ventana
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializa las vistas
        btnScan = findViewById(R.id.btnScan)
        tvResult = findViewById(R.id.tvResult)

        // Configura el clic del botón para iniciar el escáner
        btnScan.setOnClickListener {
            scanQrCodeLauncher.launch(null)
        }
    }
}

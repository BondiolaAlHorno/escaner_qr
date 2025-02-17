package com.example.escaner_qr

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanQRCode
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.example.escaner_qr.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var scanCheck:Boolean = false

    // Registra el lanzador para el escáner
    private val scanQrCodeLauncher = registerForActivityResult(ScanQRCode()) { result ->
        when (result) {
            is QRResult.QRSuccess -> {
                val scannedText = result.content.rawValue
                binding.tvResult.text = "$scannedText"
                scanCheck = true
            }
            QRResult.QRUserCanceled -> {
                binding.tvResult.text = "Escaneo cancelado por el usuario"
            }
            QRResult.QRMissingPermission -> {
                binding.tvResult.text = "Permiso de cámara no concedido"
            }
            is QRResult.QRError -> {
                binding.tvResult.text = "Error: ${result.exception.message}"
                scanCheck = true
            }
        }
    }

    private fun copyToClipboard(context: Context, text: String) {
        // Obtén el servicio del portapapeles
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        // Crea un ClipData con el texto que deseas copiar
        val clipData = ClipData.newPlainText("label", text)

        // Asigna el ClipData al portapapeles
        clipboardManager.setPrimaryClip(clipData)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Inflar el layout usando View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configura el clic del botón para iniciar el escáner
        binding.btnScan.setOnClickListener {
            scanQrCodeLauncher.launch(null)
        }

        binding.tvResult.setOnClickListener{
            if (scanCheck){
                copyToClipboard(this,binding.tvResult.text.toString())
            }
        }
    }
}

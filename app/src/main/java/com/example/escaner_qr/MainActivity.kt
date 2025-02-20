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
import androidx.core.view.WindowCompat
import com.example.escaner_qr.databinding.ActivityMainBinding
import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.appcompat.app.AlertDialog


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var scanCheck:Boolean = false

    private val wifiResult = mutableMapOf("name" to "", "password" to "")

    // Registra el lanzador para el escáner
    private val scanQrCodeLauncher = registerForActivityResult(ScanQRCode()) { result ->
        when (result) {
            is QRResult.QRSuccess -> {
                val scannedText = result.content.rawValue
                /*binding.tvResult.text = "$scannedText"*/
                scanCheck = true
                formatContent(scannedText.toString())
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

    private fun formatContent(scannedText: String) {
        if (scannedText.startsWith("WIFI:")) {
            val pattern = Regex("WIFI:S:(.*?);T:(.*?);P:(.*?);H:(.*?);;")
            val matchResult = pattern.find(scannedText)

            if (matchResult != null) {
                val (ssid, securityType, password, hidden) = matchResult.destructured
                /*binding.tvResult.text = "WIFI: $ssid \nContraseña: $password \nTipo: $securityType"*/
                binding.tvResult.text = "Tipo: $securityType"
                binding.wifiName.text = "Wifi: $ssid"
                binding.wifiPassword.text = "Contraseña: $password"
                binding.wifiPassword.visibility = View.VISIBLE
                binding.wifiName.visibility = View.VISIBLE
                wifiResult["name"] = "$ssid"
                wifiResult["password"] = "$password"
                scanCheck = false
            }
            else {
                binding.tvResult.text = scannedText
            }
        }
        else if (scannedText.startsWith("http://") || scannedText.startsWith("https://")) {
            binding.tvResult.text = scannedText
            openLinkDialog(scannedText,this)
        }
        else {
            binding.tvResult.text = scannedText
        }
    }

    private fun openLinkDialog(scannedText: String, activity: AppCompatActivity) {
        val alertDialogBuilder = AlertDialog.Builder(activity)
        alertDialogBuilder.setTitle("¿Deseas abrir el siguiente enlace?")
        alertDialogBuilder.setMessage("$scannedText")

        alertDialogBuilder.setPositiveButton("Abrir") { dialog, which ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(scannedText))
            activity.startActivity(intent)
        }
        alertDialogBuilder.setNegativeButton("Cancelar") { dialog, which ->
            dialog.dismiss()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun copyStateFalse(){
        binding.wifiPassword.visibility = View.GONE
        binding.wifiName.visibility = View.GONE
        scanCheck = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inflar el layout usando View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Asegura de que la aplicación respete los márgenes de la barra de navegación
        WindowCompat.setDecorFitsSystemWindows(window, true)

        // Configura el clic del botón para iniciar el escáner
        binding.btnScan.setOnClickListener {
            copyStateFalse()
            scanQrCodeLauncher.launch(null)
        }

        binding.tvResult.setOnClickListener{
            if (scanCheck){
                copyToClipboard(this,binding.tvResult.text.toString())
            }
        }
        binding.wifiName.setOnClickListener{
            copyToClipboard(this,wifiResult["name"].toString())
        }
        binding.wifiPassword.setOnClickListener{
            copyToClipboard(this,wifiResult["password"].toString())
        }
    }
}

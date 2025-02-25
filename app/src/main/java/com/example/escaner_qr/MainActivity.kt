package com.example.escaner_qr

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanCustomCode
import io.github.g00fy2.quickie.config.BarcodeFormat
import io.github.g00fy2.quickie.config.ScannerConfig
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.core.view.WindowCompat
import com.example.escaner_qr.databinding.ActivityMainBinding
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSuggestion
import android.provider.Settings


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var scanCheck:Boolean = false

    private val wifiResult = mutableMapOf("name" to "", "password" to "")

    private val REQUEST_CODE_LOCATION = 1001

    // Registra el lanzador para el escáner
    private val scanCustomCodeLauncher = registerForActivityResult(ScanCustomCode()) { result ->
        when (result) {
            is QRResult.QRSuccess -> {
                val scannedText = result.content.rawValue
                /*binding.qrResult.text = "$scannedText"*/
                scanCheck = true
                formatContent(scannedText.toString())
            }
            QRResult.QRUserCanceled -> {
                binding.qrResult.text = "Escaneo cancelado por el usuario"
            }
            QRResult.QRMissingPermission -> {
                binding.qrResult.text = "Permiso de cámara no concedido"
            }
            is QRResult.QRError -> {
                binding.qrResult.text = "Error: ${result.exception.message}"
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
                val (ssid, securityType, password) = matchResult.destructured
                /*binding.qrResult.text = "WIFI: $ssid \nContraseña: $password \nTipo: $securityType"*/
                binding.qrResult.text = "Tipo: $securityType"
                binding.wifiName.text = "Wifi: $ssid"
                binding.wifiPassword.text = "Contraseña: $password"
                binding.wifiPassword.visibility = View.VISIBLE
                binding.wifiName.visibility = View.VISIBLE
                wifiResult["name"] = ssid
                wifiResult["password"] = password
                scanCheck = false
                openWifiDialog(ssid,password,securityType,this)
            }
            else {
                binding.qrResult.text = scannedText
            }
        }
        else if (scannedText.startsWith("http://") || scannedText.startsWith("https://")) {
            binding.qrResult.text = scannedText
            openLinkDialog(scannedText,this)
        }
        else {
            binding.qrResult.text = scannedText
        }
    }

    private fun openLinkDialog(scannedText: String, activity: AppCompatActivity) {
        val alertDialogBuilder = AlertDialog.Builder(activity)
        alertDialogBuilder.setTitle("¿Deseas abrir el siguiente enlace?")
        alertDialogBuilder.setMessage(scannedText)

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

    private fun RequestWifiPermissions() {
        // Solicita ambos permisos
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            REQUEST_CODE_LOCATION
        )
    }

    private fun checkWifiPermissions():Boolean{
        return if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            false
        } else {
            true
        }
    }

    private fun openWifiDialog(ssid: String, password: String, securityType: String, activity: AppCompatActivity) {
        val alertDialogBuilder = AlertDialog.Builder(activity)
        alertDialogBuilder.setTitle("¿Deseas conectarse a la siguiente red WIFI?")
        alertDialogBuilder.setMessage(ssid)

        alertDialogBuilder.setPositiveButton("Conectarse") { dialog, which ->
            if (checkWifiPermissions()) {
                suggestWifiNetwork(ssid, password, securityType)
            }
            else{
                RequestWifiPermissions()
                openWifiDialog(ssid,password,securityType,this)
            }
        }
        alertDialogBuilder.setNegativeButton("Cancelar") { dialog, which ->
            dialog.dismiss()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun suggestWifiNetwork(ssid: String, password: String, securityType: String) {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        /*wifiManager.removeNetworkSuggestions(emptyList())*/ // Elimina todas las sugerencias anteriores

        val suggestion = when (securityType.uppercase()) {
            "WPA","WPA2" -> WifiNetworkSuggestion.Builder()
                .setSsid(ssid)
                .setWpa2Passphrase(password)
                .setIsAppInteractionRequired(true)
                .build()

            "WPA3" -> WifiNetworkSuggestion.Builder()
                .setSsid(ssid)
                .setWpa3Passphrase(password)
                .setIsAppInteractionRequired(true)
                .build()

            "NOPASS" -> WifiNetworkSuggestion.Builder()
                .setSsid(ssid)
                .setIsEnhancedOpen(true) // Para redes abiertas
                .setIsAppInteractionRequired(true)
                .build()

            else -> {
                return
            }
            }

        val suggestionsList = listOf(suggestion)

        // Agrega la sugerencia al sistema
        wifiManager.addNetworkSuggestions(suggestionsList)

        openWifiSettings()
    }

    private fun openWifiSettings() {
        val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
        startActivity(intent)
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
            scanCustomCodeLauncher.launch(
                ScannerConfig.build {
                    setBarcodeFormats(listOf(BarcodeFormat.FORMAT_ALL_FORMATS))
                }
            )
        }

        binding.qrResult.setOnClickListener{
            if (scanCheck){
                copyToClipboard(this,binding.qrResult.text.toString())
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

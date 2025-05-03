package com.example.alarmcontrollablebysmartphone

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.media3.common.util.UnstableApi
import com.example.alarmcontrollablebysmartphone.ui.theme.AlarmControllableBySmartphoneTheme
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

const val TAG = "BluetoothDebug"
const val TARGET_DEVICE_NAME = "RNBT-C21F"

class MainActivity : ComponentActivity() {
    lateinit var bluetoothManager: BluetoothManager
    lateinit var bluetoothAdapter: BluetoothAdapter
    var bluetoothDevice: BluetoothDevice? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBluetooth()
        enableEdgeToEdge()
        setContent {
            AlarmControllableBySmartphoneTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    @OptIn(UnstableApi::class)
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun setupBluetooth() {
        val permissions = arrayOf<String>(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        requestPermissions(permissions, 100)

        bluetoothManager = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager.adapter
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices
        pairedDevices?.forEach { device ->
            val deviceName = device.name
            val deviceHardwareAddress = device.address // MAC address
            if (deviceName == TARGET_DEVICE_NAME) {
                androidx.media3.common.util.Log.d(TAG, "name = %s, MAC <%s>".format(deviceName, deviceHardwareAddress))
                device.uuids.forEach { uuid ->
                    androidx.media3.common.util.Log.d(TAG, "uuid = %s".format(uuid.uuid))
                    bluetoothDevice = device
                    // TODO
                    return
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AlarmControllableBySmartphoneTheme {
        Greeting("Android")
    }
}
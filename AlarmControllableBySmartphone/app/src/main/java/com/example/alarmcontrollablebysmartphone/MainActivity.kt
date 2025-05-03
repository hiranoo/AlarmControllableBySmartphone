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
    var connectThread: ConnectThread? = null
    var connectedThread: ConnectedThread? = null

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
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

    fun manageMyConnectedSocket(socket: BluetoothSocket) {
        connectedThread = ConnectedThread(socket)
    }

    @SuppressLint("MissingPermission")
    inner class ConnectThread(device: BluetoothDevice) : Thread() {
        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            device.createRfcommSocketToServiceRecord(device.uuids[0].uuid)
        }

        override fun run() {
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter.cancelDiscovery()
            mmSocket?.let { socket ->
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                socket.connect()

                // The connection attempt succeeded. Perform work associated with
                // the connection in a separate thread.
                manageMyConnectedSocket(socket)
            }
        }

        // Closes the client socket and causes the thread to finish.
        @OptIn(UnstableApi::class)
        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                androidx.media3.common.util.Log.e(TAG, "Could not close the client socket", e)
            }
        }
    }

    inner class ConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {

        private val mmInStream: InputStream = mmSocket.inputStream
        private val mmOutStream: OutputStream = mmSocket.outputStream
        private val mmBuffer: ByteArray = ByteArray(1024) // mmBuffer store for the stream

        @OptIn(UnstableApi::class)
        override fun run() {
            val reader = BufferedReader(mmInStream.reader())
            var content: String
            try {
                content = reader.readText()
            } finally {
                reader.close()
            }
            androidx.media3.common.util.Log.d(TAG, content)
        }

        // Call this from the main activity to send data to the remote device.
        @OptIn(UnstableApi::class)
        fun write(bytes: ByteArray) {
            try {
                androidx.media3.common.util.Log.d("abcdef", "start writing")
                mmOutStream.write(bytes)
            } catch (e: IOException) {
                androidx.media3.common.util.Log.e(TAG, "Error occurred when sending data", e)
                return
            }
        }

        // Call this method from the main activity to shut down the connection.
        @OptIn(UnstableApi::class)
        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
                androidx.media3.common.util.Log.e(TAG, "Could not close the connect socket", e)
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
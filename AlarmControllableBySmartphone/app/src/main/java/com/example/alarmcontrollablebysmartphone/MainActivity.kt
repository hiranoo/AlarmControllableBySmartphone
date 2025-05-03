package com.example.alarmcontrollablebysmartphone

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.UnstableApi
import com.example.alarmcontrollablebysmartphone.ui.theme.AlarmControllableBySmartphoneTheme
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.Calendar
import java.util.Locale

const val TAG = "BluetoothDebug"
const val TARGET_DEVICE_NAME = "RNBT-C21F"

class MainActivity : ComponentActivity() {
    lateinit var bluetoothManager: BluetoothManager
    lateinit var bluetoothAdapter: BluetoothAdapter
    var bluetoothDevice: BluetoothDevice? = null
    var connectThread: ConnectThread? = null
    var connectedThread: ConnectedThread? = null
    private val handler = Handler(Looper.getMainLooper())

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBluetooth()
        enableEdgeToEdge()
        setContent {
            UIContent()
        }
    }

    @OptIn(UnstableApi::class)
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun setupBluetooth() {
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
                    connectThread = ConnectThread(device)
                    connectThread?.start()
                    Toast.makeText(this@MainActivity, "Bluetooth connection is being established...", Toast.LENGTH_SHORT).show()
                    return
                }
            }
        }
    }

    fun manageMyConnectedSocket(socket: BluetoothSocket) {
        Log.d(TAG, "manage my connect socket")
        if (connectedThread == null) {
            connectedThread = ConnectedThread(socket)
        }
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
        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the client socket", e)
            }
        }
    }

    inner class ConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {

        private val mmInStream: InputStream = mmSocket.inputStream
        private val mmOutStream: OutputStream = mmSocket.outputStream
        private val mmBuffer: ByteArray = ByteArray(1024) // mmBuffer store for the stream

        override fun run() {
            val reader = BufferedReader(mmInStream.reader())
            var content: String
            try {
                content = reader.readText()
                handler.post({

                })
            } finally {
                reader.close()
            }
            Log.d(TAG, content)
        }

        // Call this from the main activity to send data to the remote device.
        fun write(bytes: ByteArray) {
            try {
                Log.d("abcdef", "start writing")
                mmOutStream.write(bytes)
            } catch (e: IOException) {
                Log.e(TAG, "Error occurred when sending data", e)
                return
            }
        }

        // Call this method from the main activity to shut down the connection.
        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the connect socket", e)
            }
        }
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @kotlin.OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun UIContent() {
        var showArduinoStatus by remember { mutableStateOf(false) }
        AlarmControllableBySmartphoneTheme {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    CenterAlignedTopAppBar(
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.primary,
                        ),
                        title = {
                            Text(
                                text = "Alarm with opening the shutter",
                                modifier = Modifier.clickable {
                                    showArduinoStatus = !showArduinoStatus
                                }
                            )
                        }
                    )
                },
            ) {
                if (showArduinoStatus) {
                    ArduinoStatusView()
                } else {
                    AlarmView()
                }
            }
        }
    }

    @Composable
    fun ArduinoStatusView() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text("aaa")
            Button(
                onClick = { requestStatusToArduino() }
            ) {
                Text("Request status", fontSize = 30.sp)
            }
        }
    }

    private fun requestStatusToArduino() {
        connectedThread?.write("Status,;".toByteArray())
        handler.postDelayed({
            connectedThread?.run()
        }, 7000)
    }

    @kotlin.OptIn(ExperimentalMaterial3Api::class)
    private fun sendTimeToArduino(time: TimePickerState) {
        val alarmMinutes = MyTime(time.hour, time.minute).toMinutes()
        val localTime = LocalTime.now()
        val currentMinutes = MyTime(localTime.hour, localTime.minute).toMinutes()

        connectedThread?.write(("Alarm,$alarmMinutes;").toByteArray())
        handler.postDelayed({
            connectedThread?.write(("Current,$currentMinutes;").toByteArray())
        }, 1000)
    }

    @kotlin.OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun AlarmView() {
        var showMenu by remember { mutableStateOf(true) }
        var showDialWithDialogExample by remember { mutableStateOf(false) }
        var selectedTime: TimePickerState? by remember { mutableStateOf(null) }
        val formatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (showMenu) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    if (selectedTime != null) {
                        val cal = Calendar.getInstance()
                        cal.set(Calendar.HOUR_OF_DAY, selectedTime!!.hour)
                        cal.set(Calendar.MINUTE, selectedTime!!.minute)
                        cal.isLenient = false
                        Text(
                            text = formatter.format(cal.time),
                            fontSize = 50.sp,
                        )
                    } else {
                        Text(
                            text = "アラーム時刻を設定してください",
                            fontSize = 30.sp,
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                showDialWithDialogExample = true
                                showMenu = false
                            },
                        ) {
                            Text("アラーム時刻を変更")
                        }
                        Button(
                            enabled = bluetoothDevice != null && selectedTime != null,
                            onClick = { sendTimeToArduino(selectedTime!!) },
                        ) {
                            Text("送信")
                        }
                    }
                }
            } else {

            }

            when {
                showDialWithDialogExample -> DialWithDialog(
                    onDismiss = {
                        showDialWithDialogExample = false
                        showMenu = true
                    },
                    onConfirm = {
                            time ->
                        selectedTime = time
                        showDialWithDialogExample = false
                        showMenu = true
                    },
                )
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun UIContentPreview() {
        AlarmControllableBySmartphoneTheme {
            UIContent()
        }
    }
}
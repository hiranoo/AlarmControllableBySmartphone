# AlarmControllableBySmartphone
This is an Android application.
You can set the time you want and send it to another device via Bluetooth.
The device is Arduino UNO & RN-42 Bluetooth module in this app, but you can use other devices.
The source code in Arduino is not shared here.

## How to use
1. Enable Bluetooth on your android phone.
2. Do pairing before using this app.
3. Launch this app. It will crashe at the first launch because permissions not yet set, but no problem.
4. Allow Bluetooth permissions on your phone after the crash.
5. Relaunch this app, then you can use it.
6. Set time in this app.
7. Send the time via Bluetooth.
8. if you send time, then you will receive status from arduino and can see it in the other view. Click top bar to switch views.
9. In addition, the alarm time will be stored to local storage and reflected to the main view.

## Caution
Bluetooth connection will be established as soon as launch of the app.
Bluetooth connection can be failed sometimes, when the app crashes suddenly.
Then, you should relaunch the app until the connection is established.



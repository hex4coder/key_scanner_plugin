import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:key_scanner_plugin/key_scanner_plugin.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  String _currentDevices = '';

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      platformVersion = await KeyScannerPlugin.platformVersion;
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('KeyScannerBluetooth example app'),
        ),
        body: SingleChildScrollView(
          child: Center(
            child: Column(
              mainAxisSize: MainAxisSize.max,
              crossAxisAlignment: CrossAxisAlignment.center,
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Text('Running on: $_platformVersion\n'),
                StreamBuilder(
                    stream: KeyScannerPlugin.statusBluetoothStream,
                    builder: (BuildContext context, AsyncSnapshot<String> snapshot) {
                  if(snapshot.hasData) {
                    return Text(snapshot.data);
                  } else {
                    return Text("NO DATA");
                  }
                }),
                RaisedButton(onPressed: () {
                  KeyScannerPlugin.turnOnBluetooth;
                }, child: Text("Turn On Bluetooth"),),
                RaisedButton(onPressed: () {
                  KeyScannerPlugin.turnOffBluetooth;
                }, child: Text("Turn Off Bluetooth"),),
                RaisedButton(onPressed: () async {
                  try{
                    final result = await KeyScannerPlugin.getBoundedDevices;
                    result.forEach((device) {
                      print("Name :" + device['name']);
                      print("Address :" + device['address']);
                    });
                  } catch(e) {
                    print("Failed to get bounded devices : " + e.message);
                  }
                }, child: Text("Get Bounded Device's"),),
                RaisedButton(onPressed: () {
                  KeyScannerPlugin.startScan;
                }, child: Text("Scan Bluetooth"),),

                StreamBuilder(
                  stream: KeyScannerPlugin.bluetoothScannerStream,
                    builder: (BuildContext context, AsyncSnapshot<Map> snapshot) {
                  if(!snapshot.hasData) {
                    return Text("NO DATA");
                  } else {
                    Map map = snapshot.data;
                    // setState(() {
                      _currentDevices += "${map['name']}, ${map['address']} signal ${map['rssi']} dBm distance ${map['distance']} M.\r\n";
                    // });
                    return Text(_currentDevices, maxLines: 100,);
                  }
                })
              ],
            ),
          ),
        ),
      ),
    );
  }
}

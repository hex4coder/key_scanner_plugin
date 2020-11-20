
import 'dart:async';
import 'dart:collection';

import 'package:flutter/cupertino.dart';
import 'package:flutter/services.dart';

class KeyScannerPlugin {
  static const MethodChannel _channel =
      const MethodChannel('key_scanner_plugin');

  static const EventChannel _eventChannel = const EventChannel('bluetoothScannerStream');
  static const EventChannel _statusChannel = const EventChannel('statusBluetoothStream');
  static Stream<Map> _bluetoothScannerStream;
  static Stream<String> _statusBluetoothStream;

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<bool> get turnOnBluetooth async {
    final bool result = await _channel.invokeMethod('turnOnBluetooth');
    return result;
  }

  static Future<bool> get turnOffBluetooth async {
    final bool result = await _channel.invokeMethod('turnOffBluetooth');
    return result;
  }


  static Future<void> get makeMeDiscoverable async {
    await _channel.invokeMethod('makeMeDiscoverable');
  }

  static Future<void> get startScan async {
    await _channel.invokeMethod('startScan');
  }



  static Future<List<Map>> get getBoundedDevices async {
    final List<dynamic> result = await _channel.invokeMethod('getBoundedDevices');
    final res = new List<Map>.from(result);
    return res;
  }

  static Stream<Map> get bluetoothScannerStream {

    if(_bluetoothScannerStream == null) {
      _bluetoothScannerStream = _eventChannel.receiveBroadcastStream().map<Map>((event) => event);
    }

    return _bluetoothScannerStream;
  }

  static Stream<String> get statusBluetoothStream {

    if(_statusBluetoothStream == null) {
      _statusBluetoothStream = _statusChannel.receiveBroadcastStream().map<String>((event) => event);
    }

    return _statusBluetoothStream;
  }

}

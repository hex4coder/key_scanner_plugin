package com.example.key_scanner_plugin;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.util.Log;
import android.widget.Toast;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import android.Manifest;
import java.lang.Math;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.EventChannel.StreamHandler;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/** KeyScannerPlugin */
public class KeyScannerPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware, EventChannel.StreamHandler {

  // ============================================ global variables =============================
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private MethodChannel channel;
  private EventChannel eventChannel;
  private EventChannel statusChannel;
  private ArrayList<HashMap> listDevice;
  private BluetoothAdapter mBluetoothAdapter;
  private Context myContext;
  private Activity mActivity;
  private EventChannel.EventSink mEventSink;
  private EventChannel.EventSink mEventSinkStatus;
  // ================================================ end of variables global ===========================




  // ===================================================== activity aware ==================
  @Override
  public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
    mActivity = binding.getActivity();
  };

  @Override
  public  void onDetachedFromActivity() {
    mActivity = null;
  };

  @Override
  public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
    Log.d("ActivityAware", "activityReattached for config changed");
    mActivity = binding.getActivity();
  };

  @Override
  public void onDetachedFromActivityForConfigChanges() {
    Log.d("ActivityAware", "detached from activity for config changed");
    mActivity = null;
  }
  // ===================== end of activity aware ===========================================


//  broadcast receiver's
  private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
  @Override
  public void onReceive(Context context, Intent intent) {
    String action = intent.getAction();
    if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
      if(mEventSinkStatus != null) {
        mEventSinkStatus.success("Start scanning ...");
      }
    }

    if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
      Toast.makeText(myContext, "Discovery finished", Toast.LENGTH_SHORT).show();
      if(mEventSinkStatus != null) {
        mEventSinkStatus.success("Scan finished.");
      }

      // scan after finished for looping scan
      startScanning();
    }

    if(BluetoothDevice.ACTION_FOUND.equals(action)) {
      Toast.makeText(context, "Remote devices discovered", Toast.LENGTH_SHORT).show();
      BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
      int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);

      HashMap deviceMap = new HashMap();
      deviceMap.put("name", device.getName());
      deviceMap.put("rssi", rssi);
      deviceMap.put("address", device.getAddress());
      deviceMap.put("distance", getMetersFromRSSI(rssi));
      if(mEventSink != null) {
        mEventSink.success(deviceMap);
      }
    }
  }
};


  // ======================== EVENT CHANNEL FOR STREAM LISTENER HERE ======================================
  @Override
  public void onCancel(Object arguments) {
    mEventSink = null;
    Log.d("EVENT CHANNEL", "onCancel Fired");
  };

  @Override
  public void onListen(Object arguments, EventChannel.EventSink events) {
    mEventSink = events;
    Log.d("EVENT CHANNEL", "onListen Fired");
  };
  // ======================== END OF EVENT CHANNEL FOR STREAM LISTENER HERE ===============================








  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    myContext = flutterPluginBinding.getApplicationContext();
    BinaryMessenger mBinaryMessenger = flutterPluginBinding.getBinaryMessenger();
    channel = new MethodChannel(mBinaryMessenger, "key_scanner_plugin");
    eventChannel = new EventChannel(mBinaryMessenger, "bluetoothScannerStream");
    statusChannel = new EventChannel(mBinaryMessenger, "statusBluetoothStream");
    channel.setMethodCallHandler(this);
    eventChannel.setStreamHandler(this);
    statusChannel.setStreamHandler(new EventChannel.StreamHandler() {
      // ======================= STATUS CHANNEL ===============================
      @Override
      public void onCancel(Object arguments) {
        mEventSinkStatus = null;
        Log.d("STATUS CHANNEL", "onCancel Fired");
      };

      @Override
      public void onListen(Object arguments, EventChannel.EventSink events) {
        mEventSinkStatus = events;
        Log.d("STATUS CHANNEL", "onListen Fired");
      };
      // ======================= END OF STATUS CHANNEL ========================
    });

    listDevice = new ArrayList<HashMap>();

    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    if(mBluetoothAdapter != null) {
      if(mEventSinkStatus != null) {
        mEventSinkStatus.success("Detected device on this phone.");
      }
    } else {
      if(mEventSinkStatus != null) {
        mEventSinkStatus.success("No bluetooth device in this device");
      }
    }

    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
    myContext.registerReceiver(mReceiver, filter);

    IntentFilter filterDiscoveryStarted = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
    IntentFilter filterDiscoveryFinished = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
    myContext.registerReceiver(mReceiver, filterDiscoveryStarted);
    myContext.registerReceiver(mReceiver, filterDiscoveryFinished);
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    if (call.method.equals("getPlatformVersion")) {
      result.success("Android " + android.os.Build.VERSION.RELEASE);
      return;
    }

    if(call.method.equals("turnOnBluetooth")) {
      result.success(turnOnBluetooth());
      return;
    }

    if(call.method.equals("turnOffBluetooth")) {
      result.success(turnOffBluetooth());
      return;
    }

    if(call.method.equals("getBoundedDevices")) {
      result.success(getBoundedDevices());
      return;
    }

    if(call.method.equals("makeMeDiscoverable")) {
      makeMeDiscoverable();
      return;
    }

    if(call.method.equals("startScan")) {
      startScanning();
      return;
    }

    result.notImplemented();

  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
    mBluetoothAdapter = null;
    myContext.unregisterReceiver(mReceiver);
    myContext = null;
  }



  // ========================================================================= OUR FUNCTION'S =======

  // nyalakan bluetooth
  boolean turnOnBluetooth() {
    makeMeDiscoverable();
    if(!mBluetoothAdapter.isEnabled()) {
      return mBluetoothAdapter.enable();
    }
    if(mEventSinkStatus != null) {
      mEventSinkStatus.success("Bluetooth On");
    }

    return false;
  }

  // matikan
  boolean turnOffBluetooth() {
    if(mEventSinkStatus != null) {
      mEventSinkStatus.success("Bluetooth Off");
    }
    if(mBluetoothAdapter.isEnabled()) {
      return mBluetoothAdapter.disable();
    }

    return false;
  }

  void makeMeDiscoverable() {
    Intent discoverableIntent =
            new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
    // add extra time for scanning nearby devices, in this case is 300 seconds or 5 minutes
    discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
    mActivity.startActivity(discoverableIntent);
  }

  void requestLocation() {
    if(mActivity != null) {
      boolean isFine = mActivity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
      boolean isCoarse = mActivity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

      if(!isFine && !isCoarse) {
        mActivity.requestPermissions(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        }, 333);
      }
    }
  }

  void startScanning() {
    // FIRST : Check location permission
    requestLocation();

    // Check bluetooth device, is exists on this device
    if(mBluetoothAdapter != null) {

      // check bluetooth state
      if(!mBluetoothAdapter.isEnabled()) {
        mBluetoothAdapter.enable();
      }

      // check discovery state
      if(mBluetoothAdapter.isDiscovering()) {
        mBluetoothAdapter.cancelDiscovery();
      }

      // start discovery mode / scanning
      try {
        boolean isStarted = mBluetoothAdapter.startDiscovery();

        if(isStarted) {
          Toast.makeText(myContext, "Scanning progress ...", Toast.LENGTH_SHORT).show();
        } else {
          Toast.makeText(myContext, "Failed to start discovery process", Toast.LENGTH_SHORT).show();
        }
      } catch (Exception e) {
        Log.d("FAILED", "FAILED DISCOVERY PROCESS : " + e.getMessage());
      }
    }
  }
  
  double getMetersFromRSSI(int rssi) {
    final int measuredPower = -69;
    final int consideredLowStrengthAS = 2;
    final double distanceInMeter = Math.pow(10.0, ((measuredPower - rssi)/(10 * consideredLowStrengthAS)));
    return distanceInMeter;
  }


  ArrayList<HashMap> getBoundedDevices() {
    Set<BluetoothDevice> deviceObjects = mBluetoothAdapter.getBondedDevices();
    ArrayList<HashMap> listMap = new ArrayList<HashMap>();
    BluetoothDevice[] devices = deviceObjects.toArray(new BluetoothDevice[0]);
    for (int i = 0; i < devices.length; i++) {
      BluetoothDevice device = devices[i];
      Log.d("android mode", "the device name is : " + device.getName());
      HashMap hashMap  =new HashMap();
      hashMap.put("name", device.getName());
      hashMap.put("address", device.getAddress());
      listMap.add(hashMap);
    }

    return listMap;
  }

  // ================================================================ END OF OUR FUNCTION'S ===============
}

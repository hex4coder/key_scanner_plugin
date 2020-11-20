import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:key_scanner_plugin/key_scanner_plugin.dart';

void main() {
  const MethodChannel channel = MethodChannel('key_scanner_plugin');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await KeyScannerPlugin.platformVersion, '42');
  });
}

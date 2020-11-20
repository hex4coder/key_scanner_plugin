#import "KeyScannerPlugin.h"
#if __has_include(<key_scanner_plugin/key_scanner_plugin-Swift.h>)
#import <key_scanner_plugin/key_scanner_plugin-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "key_scanner_plugin-Swift.h"
#endif

@implementation KeyScannerPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftKeyScannerPlugin registerWithRegistrar:registrar];
}
@end

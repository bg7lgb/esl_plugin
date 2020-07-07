import 'dart:async';

import 'package:flutter/services.dart';

class EslPlugin {
  static const MethodChannel _channel =
      const MethodChannel('esl_plugin');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
}

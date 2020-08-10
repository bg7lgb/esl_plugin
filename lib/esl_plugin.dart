import 'dart:async';
import 'dart:typed_data';

import 'package:flutter/services.dart';

// 插件需要实现以下几个功能：
// 1、检测是否支持NFC功能
// 2、打开NFC读取模式
// 3、关闭NFC读取模式
// 4、发现NFC标签，判断标签（通过payload)，写入bmp文件（此部分要调用厂家提供的jar包）

typedef void EventHandler(Object event);

class EslPlugin {
  static const MethodChannel _channel =
      const MethodChannel('esl_plugin');
  static const EventChannel _stream = const EventChannel('esl_plugin_stream');


  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  /// 获取是否支持NFC功能
  /// 返回：bool true 支持， false 不支持
  static Future<bool> get isNFCSupported async {
    final supported = await _channel.invokeMethod('isNFCSupported');
    assert(supported is bool);
    return supported as bool;
  }

  /// 打开NFC功能，开始读取
  static Future<void> startReadingNFC() async {
    await _channel.invokeMethod('startReadingNFC');
  }

  /// 停止NFC读取
  static Future<void> stopReadingNFC() async {
    await _channel.invokeMethod("stopReadingNFC");
  }

  /// 发送Bmp文件，文件已经转码为Uint8List
  static Future<int> sendBmpFile(Uint8List bmp) async {
    return  await _channel.invokeMethod("sendBmp", {"bmp": bmp});
  }

  static listenNFCEvent(EventHandler onEvent, EventHandler onError) {
    _stream.receiveBroadcastStream().listen(onEvent, onError: onError);
  }
}

package com.bg7lgb.esl_plugin;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.HashMap;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.EventChannel.EventSink;
import io.flutter.plugin.common.EventChannel.StreamHandler;
//import waveshare.feng.nfctag.activity.a;

/** EslPlugin */
public class EslPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware,
        NfcAdapter.ReaderCallback, StreamHandler {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private MethodChannel channel;
  private EventChannel eventChannel;
  private Activity activity;
  private NfcAdapter adapter;
  private String LOG_TAG = "EslPlugin";
  private EventSink event;
  private Result result;
  private Tag lastTag = null;

  private static final String NORMAL_READER_MODE = "normal";
  private static final String DISPATCH_READER_MODE = "dispatch";
  private final int DEFAULT_READER_FLAGS = NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_NFC_B | NfcAdapter.FLAG_READER_NFC_F | NfcAdapter.FLAG_READER_NFC_V;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    Log.d(LOG_TAG, "onAttachedToEngine");
    channel = new MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "esl_plugin");
    channel.setMethodCallHandler(this);

    eventChannel = new EventChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "esl_plugin_stream");
    eventChannel.setStreamHandler(this);
  }

  // This static function is optional and equivalent to onAttachedToEngine. It supports the old
  // pre-Flutter-1.12 Android projects. You are encouraged to continue supporting
  // plugin registration via this function while apps migrate to use the new Android APIs
  // post-flutter-1.12 via https://flutter.dev/go/android-project-migration.
  //
  // It is encouraged to share logic between onAttachedToEngine and registerWith to keep
  // them functionally equivalent. Only one of onAttachedToEngine or registerWith will be called
  // depending on the user's project. onAttachedToEngine or registerWith must both be defined
  // in the same class.
  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "esl_plugin");
    channel.setMethodCallHandler(new EslPlugin());
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    if (call.method.equals("getPlatformVersion")) {
      result.success("Android " + android.os.Build.VERSION.RELEASE);
    } else if (call.method.equals("isNFCSupported")) {
      result.success(nfcIsEnabled());
    } else  if (call.method.equals("startReadingNFC")) {
      startReadingNFC();
    } else if (call.method.equals("stopReadingNFC")) {
      stopReadingNFC();
    } else if (call.method.equals("sendBmp")) {
      Log.d(LOG_TAG, "send bmp");
      HashMap sendArgs = call.arguments();
      if (sendArgs == null) {
        result.error("sendBmp", "missing arguments", null);
        return;
      }

      byte[] image_bytes = (byte[]) sendArgs.get("bmp");
      if (image_bytes == null) {
        result.error("sendBmp", "no image data", null);
        return;
      }
      int len = image_bytes.length;
      Bitmap bitmap = BitmapFactory.decodeByteArray(image_bytes, 0, len);
      send_bmp_file(bitmap);
      result.success(1);
//      int rt  = send_bmp_file(bitmap);
//      if (rt == 1) {
//        result.success(rt);
//      }
//      else {
//        result.error("sendBmp", "fail", null);
//      }
    } else {
      result.notImplemented();
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    Log.d(LOG_TAG, "onDetachedFromEngine");
    channel.setMethodCallHandler(null);
    eventChannel.setStreamHandler(null);
  }

  @Override
  public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
    Log.d(LOG_TAG, "onAttachedToActivity");
    activity = binding.getActivity();
  }

  @Override
  public void onReattachedToActivityForConfigChanges(ActivityPluginBinding binding) {
    Log.d(LOG_TAG, "onReattachedToActivityForConfigChanges");
    activity = binding.getActivity();
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
    Log.d(LOG_TAG, "onDetachedFromActivityForConfigChanges");
    activity = null;
  }

  @Override
  public  void onDetachedFromActivity() {
    Log.d(LOG_TAG, "onDetachedFromActivity");
    activity = null;
  }

  private Boolean nfcIsEnabled() {
    NfcAdapter adapter = NfcAdapter.getDefaultAdapter(activity);
    if (adapter == null) return false;
    return adapter.isEnabled();
  }

  private void startReadingNFC() {
    // using normal reader mode, not dispatch reader mode

    Log.d(LOG_TAG, "startReadingNfC");
    adapter = NfcAdapter.getDefaultAdapter(activity);
    if (adapter == null) return;
    Bundle bundle = new Bundle();
    int flags = DEFAULT_READER_FLAGS;
//    if (noSounds) {
//      flags = flags | NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS;
//    }
    adapter.enableReaderMode(activity, this, flags, bundle);
  }

  private void stopReadingNFC() {
    Log.d(LOG_TAG, "stopReadingNfC");
    adapter.disableReaderMode(activity);
  }

  @Override
  public void onTagDiscovered(Tag tag) {
    Log.d(LOG_TAG, "onTagDiscovered");
    lastTag = tag;
    Log.d(LOG_TAG, tag.toString());
    Ndef ndef = Ndef.get(tag);

    HashMap<String, String> message = new HashMap<>();
    message.put("message_type", "TAG");
    message.put("message_content", "TAG_DISCOVERED");
    if (event != null) {
      eventSuccess(message);
    } else {
      Log.e(LOG_TAG, "event is null");
    }
  }

  @Override
  public void onListen(Object o, EventSink eventSink) {
    event = eventSink;
  }

  @Override
  public void onCancel(Object o) {
    event = null;
  }

  private void eventSuccess(final Object result) {
//    Log.d(LOG_TAG, result.toString());

    Handler mainThread = new Handler(activity.getMainLooper());
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        if (event != null) {
          // Event stream must be handled on main/ui thread
          event.success(result);
        }
      }
    };
    mainThread.post(runnable);
  }

  private void eventError(final String code, final String message, final Object details) {
    Handler mainThread = new Handler(activity.getMainLooper());
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        if (event != null) {
          // Event stream must be handled on main/ui thread
          event.error(code, message, details);
        }
      }
    };
    mainThread.post(runnable);
  }

//  private int send_bmp_file(Bitmap bmpfile){
//    final A a = new A();//创建发送的实例。
//    NfcA nfca = NfcA.get(this.lastTag);
////    a.a(nfca);    // 初始化
//
//    Thread thread = new Thread(new Runnable() {//创建线程
//      @Override
//      public void run() {
//        int EPD_total_progress = 0;
//        HashMap<String, String> message = new HashMap<>();
//
//        while(EPD_total_progress != -1){
//
//          EPD_total_progress = a.b();//读取进度
//
//          if(EPD_total_progress == -1){
//            message.put("message_type", "PROGRESS");
//            message.put("message_content", String.valueOf(EPD_total_progress));
////            eventSuccess((Integer)EPD_total_progress);
//            eventSuccess(message);
//            break;
//          }
//
////          if(EPD_total_progress == 100 ){
//////            Log.d(LOG_TAG, "progress: " + EPD_total_progress);
////            message.put("message_type", "PROGRESS");
////            message.put("message_content", String.valueOf(EPD_total_progress));
//////            eventSuccess((Integer)EPD_total_progress);
////            eventSuccess(message);
//////            eventSuccess((Integer)EPD_total_progress);
////            break;
////          }
//
////          eventSuccess((Integer)EPD_total_progress);
//          message.put("message_type", "PROGRESS");
//          message.put("message_content", String.valueOf(EPD_total_progress));
//          eventSuccess(message);
//          if (EPD_total_progress == 100) break;
////          Log.d(LOG_TAG, "progress: " + EPD_total_progress);
//          SystemClock.sleep(100);//防止过度占用CPU
//        }
//      }
//    });
//    thread.start();
//    return  a.a(nfca, 3, bmpfile);//发送函数
////    return a.a(3, bmpfile);
//  }

  private void send_bmp_file(final  Bitmap bmpfile){
    Thread t = new Thread(new Runnable() { //创建线程
      @Override
      public void run() {
//        Boolean success = false;
        NfcA tntag;//NFC接口
        final HashMap<String, String> message = new HashMap<>();

        final A  a = new A();//创建发送的实例。
        a.a();//初始化发送函数

        Thread thread = new Thread(new Runnable() {//创建线程
          @Override
          public void run() {
            int EPD_total_progress = 0;
            while(EPD_total_progress != -1){
              EPD_total_progress = a.b();   //读取进度
              if (EPD_total_progress == -1){
                eventError("-1", "获取BMP文件发送进度出错", null );
                break;
              }

              // 向flutter发送进度
              message.put("message_type", "PROGRESS");
              message.put("message_content", String.valueOf(EPD_total_progress));
              eventSuccess(message);

              if(EPD_total_progress == 100 ){
                break;
              }
              SystemClock.sleep(100);//防止过度占用CPU
            }
          }
        });
        thread.start();    //开启获取进度线程
        tntag = NfcA.get(lastTag);//获取给定标签的实例
        try {
          a.a(tntag, 3, bmpfile);//发送函数
        } finally {
          try {
            tntag.close();
          } catch (IOException e) {//发送异常处理  NFC I/O异常
            e.printStackTrace();
          }
        }
      }
    });
    t.start(); //开启线程
  }
}

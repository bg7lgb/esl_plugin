import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'dart:async';
import 'package:dio/dio.dart';
import 'package:network_image_to_byte/network_image_to_byte.dart';
import 'dart:typed_data';

import 'package:flutter/services.dart';
import 'package:esl_plugin/esl_plugin.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  String _NFCSupported = 'Unknown';
  bool _supported = false;
  double _progress = 0.0;
  ByteData byteData;
  int _index = -1;
  String _imageUrl = "";
  Uint8List _imageBytes = null;

  final List<Goods> goodsList = [
    Goods('老干妈香辣牛肉酱', '7000189', '单位：件 ', '规格：24瓶/件'),
    Goods('统一老坛酸菜牛肉杯面（150克）', '7000294', '单位：件', '规格：10杯/件，环保纸盒包装'),
  ];

  @override
  void initState() {
    super.initState();
    initPlatformState();
    isNFCSupported();
    EslPlugin.listenNFCEvent(_onMessageEvent, _onMessageError);
  }

  @override
  void dispose() {
    print("dispose");
    if (_supported) stopReadingNFC();
    super.dispose();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      platformVersion = await EslPlugin.platformVersion;
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

  Future<void> isNFCSupported() async {
    String message;

    try {
      _supported = await EslPlugin.isNFCSupported;
      if (_supported) {
        print(_supported);
        message = "NFC功能已打开";
        EslPlugin.startReadingNFC();
      } else {
        message = "NFC功能未打开";
      }
    } on PlatformException {
      message = "获取NFC功能状态失败。";
    }

    if (!mounted) return;

    setState(() {
      _NFCSupported = message;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Column(
          children: <Widget>[
            Expanded(
              child: ListView.builder(
                  itemCount: goodsList.length,
                  itemBuilder: (context, index) {
                    return ListTile(
                      title: Text('${goodsList[index].name}'),
                      subtitle: Text('${goodsList[index].id}, ${goodsList[index]
                          .unit}, ${goodsList[index].spec}'),
                      onTap: () {
                        setState(() {
                          _index = index;
                        });
                        getEslImage(index);
                      },
                    );
                  }),
            ),
            _buildEslImage(context),
            Text('$_NFCSupported'),
            LinearProgressIndicator(
              backgroundColor: Colors.grey[200],
              valueColor: AlwaysStoppedAnimation(Colors.blue),
              value: _progress,
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildEslImage(BuildContext context) {
    if (_imageBytes == null)
      return Container(height: 0,);
    else {
      return Container(
        width: 400,
        height: 300,
        child: Image.memory(_imageBytes),
        decoration: BoxDecoration(
          border: Border.all(),
        ),
        margin: EdgeInsets.all(5.0),
      );
    }
  }

//  void onClickGoods(int index) async {
//    if (_imageBytes != null) {
//      setState(() {
//        _imageUrl = "http://192.168.2.135:5000/" + imageUrl;
//      });
//    }
//  }

  /// 获取标签图片的下载链接
  Future<void> getEslImage(int index) async {
//    String url = "http://192.168.2.135:5000";
    String url = "http://esl.snfood.com.cn";
    String api = "/esl";

    Dio dio = Dio();
    var requests_data = {};

    requests_data['goods_name'] = goodsList[index].name;
    requests_data['goods_id'] = goodsList[index].id;
    requests_data['unit'] = goodsList[index].unit;
    requests_data['spec'] = goodsList[index].spec;


    print(requests_data);
    try {
      var response = await dio.post(url + api, data: requests_data);
      if (response.data['status']) {
//        return response.data['image'];
        _imageUrl = url + '/' + response.data['image'];
        var imageBytes = await networkImageToByte(_imageUrl);
        setState(()  {
          _imageBytes = imageBytes;
        });

      }

    } on DioError catch(e) {
      print (e.message);
    }
  }

  Future<void> startReadingNFC() async {
    await EslPlugin.startReadingNFC();
  }

  Future<void> stopReadingNFC() async {
    await EslPlugin.stopReadingNFC();
  }

  // 接收到数据的处理方法
  void _onMessageEvent(Object event) {
    Map<String, String> message = Map.castFrom(event);

    if (message['message_type'] == 'PROGRESS') {
      double progress = int.parse(message['message_content']) / 100.0;
      setState(() {
        _progress = progress;
      });
    } else if (message['message_type'] == 'TAG') {
      getImageData();
    }
  }

  void getImageData() async {
//      final ByteData data = await rootBundle.load('images/demo_label.bmp');
//      final Uint8List bmplist = data.buffer.asUint8List();
      if (_imageBytes != null)
          EslPlugin.sendBmpFile(_imageBytes);
  }

  // 接收到错误的处理方法
  void _onMessageError(Object event) {
    print(event);
  }
}

class ImageAsset extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    AssetImage assetImage = AssetImage('images/demo_label.bmp');
    Image image = Image(image: assetImage,);
    return Container(
        child: image,
        padding: EdgeInsets.all(10.0),
        decoration: BoxDecoration(
          color: Colors.grey,
        ),);
  }
}

class Goods {
  String name;
  String id;
  String unit;
  String spec;

  Goods(String name, String id, String unit, String spec) {
    this.name = name;
    this.id = id;
    this.unit = unit;
    this.spec = spec;
  }
}
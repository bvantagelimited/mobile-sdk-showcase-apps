import 'dart:async';

import 'package:flutter/material.dart';

class SuccessScreen extends StatefulWidget {
  final String responseMessage;
  const SuccessScreen({super.key, required this.responseMessage});
  @override
  _SuccessScreenState createState() => _SuccessScreenState();
}

class _SuccessScreenState extends State<SuccessScreen> {
  
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('IPification Demo App'),
      ),
      body: ConstrainedBox(
          constraints: const BoxConstraints.expand(),
          child: Stack(children: [
            Align(
              alignment: Alignment.center,
              child: Column(
                  mainAxisAlignment: MainAxisAlignment.start,
                  crossAxisAlignment: CrossAxisAlignment.center,
                  children: <Widget>[
                    Padding(
                        padding: EdgeInsets.all(10),
                        child: Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            SizedBox(height: 50),
                            Image.asset(
                              'assets/tick.png',
                              width: 150,
                              height: 150,
                            ),
                            SizedBox(height: 50),
                            Text(widget.responseMessage)
                          ],
                        ))
                  ]),
            ),
            Align(
                alignment: Alignment.bottomCenter,
                child: Padding(
                  padding: EdgeInsets.all(20),
                  child: Text('Powered By : IPification',
                      textAlign: TextAlign.center),
                ))
          ])),
    );
  }
  void nextPage() {}

  void showMessage(String message) {
    // if (!mounted) return;
    // setState(() {
    //   responseMessage = message;
    // });
  }

  Future<void> startFlow() async {
    
  }
}

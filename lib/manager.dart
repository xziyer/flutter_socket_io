import 'dart:async';

import 'package:flutter/services.dart';
import 'package:adhara_socket_io/socket.dart';
import 'package:flutter/foundation.dart';


class SocketIOManager {
  static const MethodChannel _channel = const MethodChannel('adhara_socket_io');

  Map<int, SocketIO> _sockets = {};

  ///Create a [SocketIO] instance
  ///[uri] - Socket Server URL
  ///[query] - Query params to send to server as a Map
  ///returns [SocketIO]
  Future<SocketIO> createInstance(
      String uri,
      {
        Map<String, String> query,
        bool enableLogging: false,
        bool enforceNew: false
      }) async {
    String identifyingURI = {'uri': uri, 'query': query}.toString();
    /*if(!enforceNew){
//      _channel.invokeMethod('fetchInstance');
      for(int i=0; i<_sockets.length; i++){
        SocketIO socket = _sockets.values.toList()[i];
        if(socket.identifyingURI == identifyingURI){
          return socket;
        }
      }
    }*/
    int index = await _channel.invokeMethod('newInstance',
        {'uri': uri, 'query': query, 'enableLogging': enableLogging});
    SocketIO socket = SocketIO(index, identifyingURI);
    _sockets[index] = socket;
    print("sockets");
    print(_sockets.length);
    print(index);
    return socket;
  }

  ///Disconnect a socket instance and remove from stored sockets list
  Future clearInstance(SocketIO socket) async {
    await _channel.invokeMethod('clearInstance', {'id': socket.id});
    _sockets.remove(socket.id);
  }

  Future dispose() async {
    print(_sockets);
    List<Future> f = [];
    _sockets.forEach((int socketID, SocketIO socket) async {
      print(socket);
      f.add(clearInstance(socket));
    });
    await Future.wait(f);
  }

}

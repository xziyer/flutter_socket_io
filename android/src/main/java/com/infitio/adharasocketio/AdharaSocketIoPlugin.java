package com.infitio.adharasocketio;

import android.util.Log;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;


/**
 * AdharaSocketIoPlugin
 */
public class AdharaSocketIoPlugin implements MethodCallHandler {

    private List<AdharaSocket> instances;
//    private final MethodChannel channel;
    private final Registrar registrar;
    private static final String TAG = "Adhara:SocketIOPlugin";
    boolean enableLogging = false;

    private void log(Object message){
        if(this.enableLogging){
            Log.d(TAG, message.toString());
        }
    }

    private AdharaSocketIoPlugin(Registrar registrar/*, MethodChannel channel*/) {
        this.instances = new ArrayList<>();
//        this.channel = channel;
        this.registrar = registrar;
    }

    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "adhara_socket_io");
        channel.setMethodCallHandler(new AdharaSocketIoPlugin(registrar/*, channel*/));
    }

    static String[] getStringArray(List<String> arr){
        String[] str = new String[arr.size()];
        for (int j = 0; j < arr.size(); j++) {
            str[j] = arr.get(j);
        }
        return str;
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        AdharaSocket adharaSocket = null;
        if(call.hasArgument("id")){
            Integer socketIndex = call.argument("id");
            if(socketIndex!=null) {
                if (instances.size() > socketIndex) {
                    adharaSocket = instances.get(socketIndex);
                }
            }
        }
        switch (call.method) {
            case "newInstance": {
                try{
                    if(call.hasArgument("enableLogging")){
                        this.enableLogging = call.argument("enableLogging");
                    }
                    int newIndex = instances.size();
                    AdharaSocket.Options options = new AdharaSocket.Options(newIndex, (String)call.argument("uri"), (String)call.argument("namespace"));
                    try {
                        List<String> transports = call.argument("transports");
                        if (transports != null) {
                            options.transports = AdharaSocketIoPlugin.getStringArray(transports);
                        }
                        options.timeout = (Integer) call.argument("timeout");
                    }catch (Exception e){
                        Log.e(TAG, e.toString());
                    }
                    if(call.hasArgument("query")) {
                        Map<String, String> _query = call.argument("query");
                        if(_query!=null) {
                            StringBuilder sb = new StringBuilder();
                            for (Map.Entry<String, String> entry : _query.entrySet()) {
                                sb.append(entry.getKey());
                                sb.append("=");
                                sb.append(entry.getValue());
                                sb.append("&");
                            }
                            options.query = sb.toString();
                        }
                    }
                    options.enableLogging = this.enableLogging;
                    this.instances.add(AdharaSocket.getInstance(registrar, options));
                    result.success(newIndex);
                }catch (URISyntaxException use){
                    result.error(use.toString(), null, null);
                }
                break;
            }
            case "clearInstance": {
                if(adharaSocket==null){
                    result.error("Invalid instance identifier provided", null, null);
                    break;
                }
                this.instances.remove(adharaSocket);
                adharaSocket.socket.disconnect();
                result.success(null);
                break;
            }
            default: {
                result.notImplemented();
            }
        }
    }

}

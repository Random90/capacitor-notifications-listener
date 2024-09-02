package com.capacitor.notifications.listener;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;

import org.json.JSONArray;

import java.util.Objects;

@CapacitorPlugin(
        name = "NotificationsListener",
        permissions = {
                @Permission(
                        alias = "notifications",
                        strings = { Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE }
                )
        }

)

public class NotificationsListenerPlugin extends Plugin {
    private static final String TAG = NotificationsListenerPlugin.class.getSimpleName();
    private static final String EVENT_NOTIFICATION_REMOVED = "notificationRemovedEvent";
    private static final String EVENT_NOTIFICATION_RECEIVED = "notificationReceivedEvent";

    private NotificationReceiver notificationreceiver;

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @PluginMethod()
    public void startListening(PluginCall call) {
        if (notificationreceiver != null) {
            call.resolve();
            Log.d(TAG, "NotificationReceiver already exists");
            return;
        }
        notificationreceiver = new NotificationReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(NotificationService.ACTION_RECEIVE);
        filter.addAction(NotificationService.ACTION_REMOVE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            getContext().registerReceiver(notificationreceiver, filter);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getContext().registerReceiver(notificationreceiver, filter, Context.RECEIVER_EXPORTED);
        }
        call.resolve();
    }

    @PluginMethod
    public void requestPermission(PluginCall call) {
        startActivityForResult(call, new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS), 0);
        call.resolve();
    }

    @PluginMethod
    public void isListening(PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("value",  NotificationService.isConnected);
        call.resolve(ret);
    }

    @PluginMethod()
    public void stopListening(PluginCall call) {
        if (notificationreceiver == null) {
            call.resolve();
            return;
        }
        getContext().unregisterReceiver(notificationreceiver);
        call.resolve();
    }

    private class NotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            JSObject jo = new JSObject();
            try {
                jo.put("apptitle", intent.getStringExtra(NotificationService.ARG_APPTITLE));
                jo.put("text", intent.getStringExtra(NotificationService.ARG_TEXT));
                JSONArray ja = new JSONArray();
                for (String k : Objects.requireNonNull(intent.getStringArrayExtra(NotificationService.ARG_TEXTLINES)))
                    ja.put(k);
                jo.put("textlines", ja.toString());
                jo.put("title", intent.getStringExtra(NotificationService.ARG_TITLE));
                jo.put("time", intent.getLongExtra(NotificationService.ARG_TIME, System.currentTimeMillis()));
                jo.put("package", intent.getStringExtra(NotificationService.ARG_PACKAGE));
            } catch (Exception e) {
                Log.e(TAG, "JSObject Error");
                return;
            }
            switch (Objects.requireNonNull(intent.getAction())){
                case NotificationService.ACTION_RECEIVE:
                    notifyListeners(EVENT_NOTIFICATION_RECEIVED, jo);
                    break;
                case NotificationService.ACTION_REMOVE :
                    notifyListeners(EVENT_NOTIFICATION_REMOVED, jo);
                    break;
            }
        }
    }
}

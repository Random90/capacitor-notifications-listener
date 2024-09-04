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

import java.util.ArrayList;
import java.util.Objects;
import org.json.JSONArray;

@CapacitorPlugin(
    name = "NotificationsListener",
    permissions = { @Permission(alias = "notifications", strings = { Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE }) }
)
public class NotificationsListenerPlugin extends Plugin {

    private static final String TAG = NotificationsListenerPlugin.class.getSimpleName();
    private static final String EVENT_NOTIFICATION_REMOVED = "notificationRemovedEvent";
    private static final String EVENT_NOTIFICATION_RECEIVED = "notificationReceivedEvent";

    private Boolean cacheEnabled = false;
    private Boolean webViewActive = true;
    private final ArrayList<JSObject> notificationsCache = new ArrayList<>();

    private NotificationReceiver notificationReceiver;

    public void load() {
        bridge.getApp().setStatusChangeListener((isActive) -> {
            Log.d(TAG, "App is now " + (isActive ? "active" : "inactive"));
            Log.d(TAG, "cache size: " + notificationsCache.size());
            if (notificationReceiver != null) {
                notificationReceiver.setWebViewActive(isActive);
            }
            if (isActive) {
                webViewActive = true;

                if (cacheEnabled) {
                    for (JSObject jo : notificationsCache) {
                        Log.d(TAG, "cache size: " + notificationsCache.size());
                        Log.d(TAG, "Restoring cached notification: " + jo.toString());
                        notifyListeners(EVENT_NOTIFICATION_RECEIVED, jo);
                    }
                    notificationsCache.clear();
                    Log.d(TAG, "cache size: " + notificationsCache.size());
                }
            } else {
                webViewActive = false;
            }
        });
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @PluginMethod
    public void startListening(PluginCall call) {
        cacheEnabled = call.getBoolean("cacheNotifications", false);
        Log.d(TAG, "Cache enabled: " + cacheEnabled + " notificationReceiver exists: " + (notificationReceiver != null));
        if (notificationReceiver != null) {
            notificationReceiver.setCacheEnabled(cacheEnabled);
            call.resolve();
            Log.d(TAG, "NotificationReceiver already exists");
            return;
        }
        notificationReceiver = new NotificationReceiver(cacheEnabled, webViewActive);
        IntentFilter filter = new IntentFilter();
        filter.addAction(NotificationService.ACTION_RECEIVE);
        filter.addAction(NotificationService.ACTION_REMOVE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            getContext().registerReceiver(notificationReceiver, filter);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getContext().registerReceiver(notificationReceiver, filter, Context.RECEIVER_EXPORTED);
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
        ret.put("value", NotificationService.isConnected);
        call.resolve(ret);
    }

    @PluginMethod
    public void stopListening(PluginCall call) {
        if (notificationReceiver == null) {
            call.resolve();
            return;
        }
        getContext().unregisterReceiver(notificationReceiver);
        call.resolve();
    }



    private class NotificationReceiver extends BroadcastReceiver {
        private boolean cacheEnabled;
        private boolean webViewActive;

        private NotificationReceiver(boolean cacheEnabled, boolean webViewActive) {
            this.cacheEnabled = cacheEnabled;
            this.webViewActive = webViewActive;
        }

        public void setCacheEnabled(boolean cacheEnabled) {
            this.cacheEnabled = cacheEnabled;
        }

        public void setWebViewActive(boolean webViewActive) {
            this.webViewActive = webViewActive;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            // Log all extras in the intent
//            Bundle extras = intent.getExtras();
//            if (extras != null) {
//                for (String key : extras.keySet()) {
//                    Object value = extras.get(key);
//                    if (value == null) return;
//                    Log.d(TAG, String.format("Intent extra: %s = %s (%s)", key, value.toString(), value.getClass().getName()));
//                }
//            }

//            Log.d(TAG, intent.getType() != null ? intent.getType() : "null");
//            Log.d(TAG, intent.getAction() != null ? intent.getAction() : "null");
//            Log.d(TAG, intent.getDataString() != null ? intent.getDataString() : "null");
//            Log.d(TAG, intent.getScheme() != null ? intent.getScheme() : "null");
//            Log.d(TAG, intent.getPackage() != null ? intent.getPackage() : "null");
//            Log.d(TAG, intent.getComponent() != null ? intent.getComponent().toString() : "null");
//            Log.d(TAG, intent.toString());

            JSObject jo = new JSObject();
            try {
                jo.put("apptitle", intent.getStringExtra(NotificationService.ARG_APPTITLE));
                jo.put("text", intent.getStringExtra(NotificationService.ARG_TEXT));
                JSONArray ja = new JSONArray();
                for (String k : Objects.requireNonNull(intent.getStringArrayExtra(NotificationService.ARG_TEXTLINES))) ja.put(k);
                jo.put("textlines", ja.toString());
                jo.put("title", intent.getStringExtra(NotificationService.ARG_TITLE));
                jo.put("time", intent.getLongExtra(NotificationService.ARG_TIME, System.currentTimeMillis()));
                jo.put("package", intent.getStringExtra(NotificationService.ARG_PACKAGE));
            } catch (Exception e) {
                Log.e(TAG, "JSObject Error");
                return;
            }
            switch (Objects.requireNonNull(intent.getAction())) {
                case NotificationService.ACTION_RECEIVE:
                    // log original intent
                    Log.d(TAG, "Received notification: " + jo.toString());
                    Log.d(TAG, "Cache enabled: " + cacheEnabled + ", WebView active: " + webViewActive);
                    if (cacheEnabled && !webViewActive) {
                        Log.d(TAG, "Caching notification: " + jo.toString());
                        // TODO store in storage
                        notificationsCache.add(jo);
                        Log.d(TAG, "cache size: " + notificationsCache.size());
                        return;
                    }
                    notifyListeners(EVENT_NOTIFICATION_RECEIVED, jo);
                    break;
                case NotificationService.ACTION_REMOVE:
                    notifyListeners(EVENT_NOTIFICATION_REMOVED, jo);
                    break;
            }
        }
    }
}

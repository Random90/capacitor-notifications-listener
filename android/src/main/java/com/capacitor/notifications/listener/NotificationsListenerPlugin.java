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
import org.json.JSONObject;

@CapacitorPlugin(
        name = "NotificationsListener",
        permissions = {@Permission(alias = "notifications", strings = {Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE})}
)
public class NotificationsListenerPlugin extends Plugin {

    private static final String TAG = NotificationsListenerPlugin.class.getSimpleName();
    private static final String EVENT_NOTIFICATION_REMOVED = "notificationRemovedEvent";
    private static final String EVENT_NOTIFICATION_RECEIVED = "notificationReceivedEvent";

    private NotificationReceiver notificationReceiver = null;

    public void load() {
        attachAppStateListener();
        NotificationService.persistentStorage = new SimpleStorage(getContext());
        NotificationService.pluginInstance = this;
    }

    @Override
    protected void handleOnDestroy() {
        getContext().unregisterReceiver(notificationReceiver);
        NotificationService.notificationReceiver = null;
        NotificationService.pluginInstance = null;
        NotificationService.webViewActive = false;
        Log.d(TAG, "Plugin Destroyed, NotificationReceiver unregistered");

    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @PluginMethod
    public void startListening(PluginCall call) {
        Boolean cacheEnabledValue = call.getBoolean("cacheNotifications");
        ArrayList<String> packagesWhitelist = arrayFromPluginCall(call);
        NotificationService.cacheEnabled = (cacheEnabledValue != null) ? cacheEnabledValue : false;
        NotificationService.packagesWhitelist = packagesWhitelist;

        if (packagesWhitelist != null) {
            Log.d(TAG, "Listening to packages: " + packagesWhitelist);
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(NotificationService.ACTION_RECEIVE);
        filter.addAction(NotificationService.ACTION_REMOVE);
        notificationReceiver = new NotificationReceiver(getContext(), filter);
        NotificationService.notificationReceiver = notificationReceiver;
        call.resolve();
    }

    @PluginMethod
    public void restoreCachedNotifications(PluginCall call) {
        restoreFromCache();
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

    @PluginMethod
    public void replacePackagesWhiteList(PluginCall call) {
        ArrayList<String> packagesWhitelist = arrayFromPluginCall(call);
        NotificationService.packagesWhitelist = packagesWhitelist;
        if (packagesWhitelist != null) {
            Log.d(TAG, "Listening to new packages: " + packagesWhitelist.toString());
        } else {
            Log.d(TAG, "Whitelist disabled");
        }
        call.resolve();
    }

    private void restoreFromCache() {
        JSONArray persistedJSONArray = NotificationService.persistentStorage.retrieve(NotificationService.STORAGE_KEY);
        if (persistedJSONArray == null) {
            Log.d(TAG, "No cached notifications to restore");
            return;
        }
        Log.d(TAG, "Cache size: " + NotificationService.persistentStorage.size(NotificationService.STORAGE_KEY));
        try {
            for (int i = 0; i < persistedJSONArray.length(); i++) {
                JSONObject jo = persistedJSONArray.getJSONObject(i);
                Log.d(TAG, "Restoring cached notification: " + jo.toString());
                notifyListeners(EVENT_NOTIFICATION_RECEIVED, new JSObject(jo.toString()));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error restoring cached notifications");
            e.printStackTrace();
        }
        NotificationService.persistentStorage.remove(NotificationService.STORAGE_KEY);
    }

    private void attachAppStateListener() {
        bridge.getApp().setStatusChangeListener((isActive) -> {
            NotificationService.webViewActive = isActive;
            // Restore cached notifications if the webview is unpaused, but not before webView starts the listener after killing
            // restoreCachedNotifications() called from webview will handle that case.
            if (isActive && NotificationService.cacheEnabled && NotificationService.notificationReceiver != null) {
                restoreFromCache();
            }
        });
    }

    private ArrayList<String> arrayFromPluginCall(PluginCall call) {
        ArrayList<String> list = new ArrayList<>();
        JSONArray jsonArray = call.getArray("packagesWhitelist");
        if (jsonArray != null) {
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    list.add(jsonArray.getString(i));
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing packagesWhitelist entry");
                }
            }
        }
        if (list.isEmpty()) {
            return null;
        }
        return list;
    }

    public class NotificationReceiver extends BroadcastReceiver {

        @SuppressLint("UnspecifiedRegisterReceiverFlag")
        private NotificationReceiver(Context context, IntentFilter filter) {
            Log.d(TAG, "NotificationReceiver created");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(this, filter);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(this, filter, Context.RECEIVER_EXPORTED);
            }
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            JSObject jo = parseNotification(intent);
            switch (Objects.requireNonNull(intent.getAction())) {
                case NotificationService.ACTION_RECEIVE:
                    notifyListeners(EVENT_NOTIFICATION_RECEIVED, jo);
                    break;
                case NotificationService.ACTION_REMOVE:
                    notifyListeners(EVENT_NOTIFICATION_REMOVED, jo);
                    break;
            }
        }

        private JSObject parseNotification(Intent intent) {
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
                return null;
            }
            return jo;
        }
    }
}

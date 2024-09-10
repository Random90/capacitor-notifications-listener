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
    private static final String STORAGE_KEY = "notificationsCache";

    private Boolean cacheEnabled = false;
    private Boolean webViewActive = true;
    private SimpleStorage persistentStorage;

    public void load() {
        persistentStorage = new SimpleStorage(getContext());
        attachAppStateListener();
    }

    @Override
    protected void handleOnDestroy() {
        // TODO also remove listeners?
        Log.d(TAG, "Destroyed");
    }

    // TODO: test if reinstalling the app will properly register new listener
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @PluginMethod
    public void startListening(PluginCall call) {
        Boolean cacheEnabledValue = call.getBoolean("cacheNotifications");
        ArrayList<String> packagesWhitelist = arrayFromPluginCall(call);
        if (packagesWhitelist != null) {
            Log.d(TAG, "Listening to packages: " + packagesWhitelist.toString());
        }

        cacheEnabled = (cacheEnabledValue != null) ? cacheEnabledValue : false;
        if (NotificationService.notificationReceiver != null) {
            Log.d(TAG, "NotificationReceiver already exists, unregistering");
            NotificationService.notificationReceiver.unregister(getContext());
        }
        NotificationService.notificationReceiver = new NotificationReceiver(cacheEnabled, webViewActive, packagesWhitelist);
        IntentFilter filter = new IntentFilter();
        filter.addAction(NotificationService.ACTION_RECEIVE);
        filter.addAction(NotificationService.ACTION_REMOVE);

        NotificationService.notificationReceiver.register(getContext(), filter);
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
        if (NotificationService.notificationReceiver == null) {
            call.resolve();
            return;
        }
        NotificationService.notificationReceiver.unregister(getContext());
        call.resolve();
    }

    @PluginMethod
    public void replacePackagesWhiteList(PluginCall call) {
        ArrayList<String> packagesWhitelist = arrayFromPluginCall(call);
        NotificationService.notificationReceiver.setPackagesWhitelist(packagesWhitelist);
        if (packagesWhitelist != null) {
            Log.d(TAG, "Listening to new packages: " + packagesWhitelist.toString());
        } else {
            Log.d(TAG, "Whitelist disabled");
        }
        call.resolve();
    }


    private void attachAppStateListener() {
        bridge.getApp().setStatusChangeListener((isActive) -> {
            if (NotificationService.notificationReceiver != null) {
                NotificationService.notificationReceiver.setWebViewActive(isActive);
            }
            if (isActive) {
                webViewActive = true;

                if (cacheEnabled) {
                    restoreFromCache();
                }
            } else {
                webViewActive = false;
            }
        });
    }

    private void restoreFromCache() {
        JSONArray persistedJSONArray = persistentStorage.retrieve(STORAGE_KEY);
        if (persistedJSONArray == null) {
            Log.d(TAG, "No cached notifications to restore");
            return;
        }
        Log.d(TAG, "Cache size: " + persistentStorage.size(STORAGE_KEY));
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
        persistentStorage.remove(STORAGE_KEY);
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
        public boolean isRegistered;
        private boolean cacheEnabled;
        private boolean webViewActive;

        private NotificationReceiver(boolean cacheEnabled, boolean webViewActive, ArrayList<String> packagesWhitelist) {
            Log.d(TAG, "NotificationReceiver created");
            this.isRegistered = false;
            this.cacheEnabled = cacheEnabled;
            this.webViewActive = webViewActive;
            NotificationService.packagesWhitelist = packagesWhitelist;
        }

        /**
         * @return see Context.registerReceiver(BroadcastReceiver,IntentFilter)
         */
        @SuppressLint("UnspecifiedRegisterReceiverFlag")
        public Intent register(Context context, IntentFilter filter) {
            try {
                if (!isRegistered) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                        return context.registerReceiver(this, filter);
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        return context.registerReceiver(this, filter, Context.RECEIVER_EXPORTED);
                    }
                }
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            } finally {
                isRegistered = true;
            }
        }

        /**
         * @return true if was registered else false
         */
        public boolean unregister(Context context) {
            // TODO hold the weak reference to old context in service, so the receiver can be properly removed.
            // currently after killing the app context is new. At least reference to the receiver is kept in the service, so unregistering it
            // stops sending data to the WebView
            return isRegistered
                    && unregisterInternal(context);
        }

        private boolean unregisterInternal(Context context) {
            try {
                context.unregisterReceiver(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
            isRegistered = false;
            return true;
        }

        public void setCacheEnabled(boolean cacheEnabled) {
            this.cacheEnabled = cacheEnabled;
        }

        public void setWebViewActive(boolean webViewActive) {
            this.webViewActive = webViewActive;
        }

        public void setPackagesWhitelist(ArrayList<String> packagesWhitelist) {
            NotificationService.packagesWhitelist = packagesWhitelist;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (!isRegistered) {
                Log.w(TAG, "Unregistered receiver is still registered in Android. Hope it kills it");
                return;
            }
            JSObject jo = parseNotification(intent);
            switch (Objects.requireNonNull(intent.getAction())) {
                case NotificationService.ACTION_RECEIVE:
                    // log original intent
                    if (cacheEnabled && !webViewActive) {
                        Log.d(TAG, "Caching notification");
                        persistentStorage.append(STORAGE_KEY, jo);
                        Log.d(TAG, "New cache size: " + persistentStorage.size(STORAGE_KEY));
                        return;
                    } else if (!cacheEnabled && !webViewActive) {
                        Log.d(TAG, "Cache disabled, not caching notification in bg");
                    }
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

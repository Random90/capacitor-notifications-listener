package com.capacitor.notifications.listener;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;

import java.util.ArrayList;
import java.util.Objects;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@CapacitorPlugin(
        name = "NotificationsListener",
        permissions = {@Permission(alias = "notifications", strings = {Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE})}
)
public class NotificationsListenerPlugin extends Plugin {

    private static final String TAG = NotificationsListenerPlugin.class.getSimpleName();
    private static final String EVENT_NOTIFICATION_REMOVED = "notificationRemovedEvent";
    private static final String EVENT_NOTIFICATION_RECEIVED = "notificationReceivedEvent";
    private static final String ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS";
    private static final String EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME = "android.provider.extra.NOTIFICATION_LISTENER_COMPONENT_NAME";

    private NotificationReceiver notificationReceiver = null;
    private SimpleStorage persistentStorage = null;

    public void load() {
        attachAppStateListener();
        NotificationService.pluginInstance = this;
    }

    @Override
    protected void handleOnDestroy() {
        this.pluginCleanup();
        Log.d(TAG, "Plugin Destroyed, NotificationReceiver unregistered");
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @PluginMethod
    public void startListening(PluginCall call) throws JSONException {
        Boolean cacheEnabledValue = call.getBoolean("cacheNotifications");
        String storageGroupName = call.getString("storageGroupName");
        ArrayList<String> packagesWhitelist = arrayFromPluginCall(call);
        persistentStorage = new SimpleStorage(getContext(), storageGroupName);
        IntentFilter filter = new IntentFilter();
        filter.addAction(NotificationService.ACTION_RECEIVE);
        filter.addAction(NotificationService.ACTION_REMOVE);
        notificationReceiver = new NotificationReceiver(getContext(), filter);

        NotificationService.init(
                persistentStorage,
                (cacheEnabledValue != null) ? cacheEnabledValue : false,
                packagesWhitelist,
                notificationReceiver,
                getContext()
        );

        if (storageGroupName != null) {
            Log.d(TAG, "Using custom storage group: " + storageGroupName);
        }

        persistentStorage.set(NotificationService.CACHE_ENABLED_STORAGE_KEY, String.valueOf(cacheEnabledValue));
        if (packagesWhitelist != null) {
            this.persistWhitelist(packagesWhitelist);
        }
        if (packagesWhitelist != null) {
            Log.d(TAG, "Listening to packages: " + packagesWhitelist);
        }
        call.resolve();
    }

    @PluginMethod
    public void restoreCachedNotifications(PluginCall call) {
        restoreFromCache();
        call.resolve();
    }

    @PluginMethod
    public void requestPermission(PluginCall call) {
        Boolean forceOpenSettings = call.getBoolean("forceOpenSettings");
        if (isNotificationListenerPermissionGranted() && (forceOpenSettings == null || !forceOpenSettings)) {
            JSObject ret = new JSObject();
            ret.put("value", true);
            call.resolve(ret);
            return;
        }
        try {
            openNotificationListenerSettings();
            JSObject ret = new JSObject();
            ret.put("value", false);
            call.resolve(ret);
        } catch (Exception e) {
            Log.e(TAG, "Error opening notification listener settings", e);
            call.reject("Unable to open notification listener settings", e);
        }
    }

    @PluginMethod
    public void isPermissionGranted(PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("value", isNotificationListenerPermissionGranted());
        call.resolve(ret);
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
    public void replacePackagesWhitelist(PluginCall call) {
        ArrayList<String> packagesWhitelist = arrayFromPluginCall(call);
        NotificationService.packagesWhitelist = packagesWhitelist;
        if (packagesWhitelist != null) {
            this.persistWhitelist(packagesWhitelist);
            Log.d(TAG, "Listening to new packages: " + packagesWhitelist.toString());
        } else {
            Log.d(TAG, "Whitelist disabled");
            persistentStorage.remove(NotificationService.WHITE_LIST_STORAGE_KEY);
        }
        call.resolve();
    }

    private void restoreFromCache() {
        JSONArray persistedJSONArray = persistentStorage.retrieve(NotificationService.NOTIFICATIONS_STORAGE_KEY);
        if (persistedJSONArray == null) {
            Log.d(TAG, "No cached notifications to restore");
            return;
        }
        Log.d(TAG, "Cache size: " + persistentStorage.size(NotificationService.NOTIFICATIONS_STORAGE_KEY));
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
        persistentStorage.remove(NotificationService.NOTIFICATIONS_STORAGE_KEY);
    }

    private void attachAppStateListener() {
        bridge.getApp().setStatusChangeListener((isActive) -> {
            NotificationService.webViewActive = isActive;
            // Restore cached notifications if the webview is unpaused, but not before webView starts the listener after killing
            // restoreCachedNotifications() called from webview will handle that case.
            if (isActive && NotificationService.cacheEnabled != null && NotificationService.cacheEnabled && NotificationService.notificationReceiver != null) {
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

    private void persistWhitelist(ArrayList<String> packagesWhitelist) {
        JSONArray jsonArrayWhitelist = new JSONArray(packagesWhitelist);
        persistentStorage.set(NotificationService.WHITE_LIST_STORAGE_KEY, jsonArrayWhitelist.toString());
    }

    private boolean isNotificationListenerPermissionGranted() {
        String enabledListeners = Settings.Secure.getString(
                getContext().getContentResolver(),
                "enabled_notification_listeners"
        );
        if (TextUtils.isEmpty(enabledListeners)) {
            return false;
        }

        ComponentName componentName = new ComponentName(getContext(), NotificationService.class);
        String flattenedComponent = componentName.flattenToString();
        for (String listener : enabledListeners.split(":")) {
            if (flattenedComponent.equals(listener)) {
                return true;
            }
        }
        return false;
    }

    private void openNotificationListenerSettings() throws ActivityNotFoundException {
        Intent detailIntent = getDetailIntent();

        try {
            getContext().startActivity(detailIntent);
            return;
        } catch (ActivityNotFoundException ignored) {
            // Fallback to generic Notification Listener screen.
            Log.w(TAG, "Unable to open notification listener settings detail screen, falling back to generic settings screen.");
        }

        Intent listIntent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
        listIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            getContext().startActivity(listIntent);
            return;
        } catch (ActivityNotFoundException ignored) {
            // Last fallback to app details page.
            Log.w(TAG, "Unable to open notification listener settings screen, falling back to app details page.");
        }

        Intent appDetailsIntent = new Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", getContext().getPackageName(), null)
        );
        appDetailsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(appDetailsIntent);
    }

    @NonNull
    private Intent getDetailIntent() {
        ComponentName componentName = new ComponentName(getContext(), NotificationService.class);

        Intent detailIntent = new Intent(ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS);
        detailIntent.putExtra(EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME, componentName.flattenToString());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            detailIntent.putExtra(Settings.EXTRA_APP_PACKAGE, getContext().getPackageName());
        }
        detailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return detailIntent;
    }

    private void pluginCleanup() {
        NotificationService.pluginInstance = null;
        NotificationService.webViewActive = false;
        if (NotificationService.notificationReceiver == null) {
            return;
        }
        try {
            getContext().unregisterReceiver(notificationReceiver);
        } catch (Exception e) {
            Log.e(TAG, "Error unregistering NotificationReceiver", e);
        }
        NotificationService.notificationReceiver = null;
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

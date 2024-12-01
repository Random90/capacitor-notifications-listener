package com.capacitor.notifications.listener;

import android.app.Notification;
import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

// TODO - autostart: persist whitelist and cachedEnabled bool on restart to continue listening. Service (at least on android 14 starts automatically)
// TODO - use DataStore or SQLite for persistent storage

public class NotificationService extends NotificationListenerService {

    public static final String NOTIFICATIONS_STORAGE_KEY = "notificationsCache";
    public static final String CACHE_ENABLED_STORAGE_KEY = "notificationsCacheEnabled";
    public static final String WHITE_LIST_STORAGE_KEY = "notificationsWhitelist";

    public static final String ACTION_RECEIVE = "com.capacitor.notifications.listener.NOTIFICATION_RECEIVE_EVENT";
    public static final String ACTION_REMOVE = "com.capacitor.notifications.listener.NOTIFICATION_REMOVE_EVENT";

    public static final String ARG_PACKAGE = "notification_event_package";
    public static final String ARG_TITLE = "notification_event_title";
    public static final String ARG_APPTITLE = "notification_event_apptitle";
    public static final String ARG_TEXT = "notification_event_text";
    public static final String ARG_TEXTLINES = "notification_event_textlines";
    public static final String ARG_TIME = " notification_event_time";

    private static final String TAG = NotificationService.class.getSimpleName();

    public static NotificationsListenerPlugin.NotificationReceiver notificationReceiver;
    // service connected to the android notification service
    public static boolean isConnected = false;
    // listening started by the webview app
    public static ArrayList<String> packagesWhitelist = null;
    public static Plugin pluginInstance;
    public static Boolean cacheEnabled = null;
    public static boolean webViewActive = false;

    private SimpleStorage persistentStorage;
    private StatusBarNotification lastNotification;

    private final UUID uuid;

    public NotificationService() {
        uuid = UUID.randomUUID();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Spawning NotificationService with UUID: " + uuid);
        persistentStorage = new SimpleStorage(getApplicationContext());
        packagesWhitelist = persistentStorage.retrieveArrayList(WHITE_LIST_STORAGE_KEY);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.d(TAG, "Service ID" + this.uuid + " Whitelist size: " + packagesWhitelist.size() + " Receiver: " + notificationReceiver + " WebViewActive: " + webViewActive);
        Log.d(TAG, "Received notification: " + sbn.getNotification().extras.getCharSequence("android.text"));
        if (!existsInWhitelist(sbn)) return;
        // workaround for duplicate notifications on older android versions after app is killed by force
        if (lastNotification != null)
        {
            boolean sameText = Objects.equals(lastNotification.getNotification().extras.getCharSequence("android.text"), sbn.getNotification().extras.getCharSequence("android.text"));
            boolean sameTime = lastNotification.getNotification().when == sbn.getNotification().when;
            if (sameText && sameTime) {
                Log.d(TAG, "Notification already received - skipping");
                return;
            }
        }
        lastNotification = sbn;
        if (cacheEnabled == null) {
            cacheEnabled = Boolean.parseBoolean(persistentStorage.get(CACHE_ENABLED_STORAGE_KEY));
        }
        if ((notificationReceiver == null || !webViewActive) && cacheEnabled) {
            persistentStorage.append(NOTIFICATIONS_STORAGE_KEY, notificationToJSObject(sbn));
            Log.d(TAG, "Notification cached. New size: " + persistentStorage.size(NOTIFICATIONS_STORAGE_KEY));
            return;
        }
        if (notificationReceiver == null) {
            Log.w(TAG, "NotificationReceiver not created and cache is disabled - notification skipped");
            return;
        }

        Log.d(TAG, "Sending notification to webview");
        Intent i = notificationToIntent(sbn, ACTION_RECEIVE);
        sendBroadcast(i);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        if (notificationReceiver == null || !webViewActive) return;
        if (packagesWhitelist == null || existsInWhitelist(sbn)) {
            Intent i = notificationToIntent(sbn, ACTION_REMOVE);
            sendBroadcast(i);
        }
    }

    @Override
    public void onListenerConnected() {
        isConnected = true;
    }

    @Override
    public void onListenerDisconnected() {
        isConnected = false;
    }

    private Intent notificationToIntent(StatusBarNotification sbn, String action) {
        Intent i = new Intent(action);
        Notification n = sbn.getNotification();

        CharSequence pkg = sbn.getPackageName();
        i.putExtra(ARG_PACKAGE, charSequenceToString(pkg));

        CharSequence title = n.tickerText;
        i.putExtra(ARG_TITLE, charSequenceToString(title));

        CharSequence text = n.extras.getCharSequence("android.text");
        i.putExtra(ARG_TEXT, charSequenceToString(text));

        CharSequence[] textlines = n.extras.getCharSequenceArray("android.textLines");
        i.putExtra(ARG_TEXTLINES, charSequenceArrayToStringArray(textlines));

        CharSequence apptitle = n.extras.getCharSequence("android.title");
        i.putExtra(ARG_APPTITLE, charSequenceToString(apptitle));

        i.putExtra(ARG_TIME, n.when);
        return i;
    }

    private JSObject notificationToJSObject(StatusBarNotification sbn) {
        JSObject jo = new JSObject();
        Notification n = sbn.getNotification();

        CharSequence pkg = sbn.getPackageName();
        jo.put("package", charSequenceToString(pkg));

        CharSequence title = n.tickerText;
        jo.put("title", charSequenceToString(title));

        CharSequence text = n.extras.getCharSequence("android.text");
        jo.put("text", charSequenceToString(text));

        // TODO fix textlines not converted properly to JSArray
        CharSequence[] textlines = n.extras.getCharSequenceArray("android.textLines");
        jo.put("textlines", stringArrayToJSONArray(charSequenceArrayToStringArray(textlines)));

        CharSequence apptitle = n.extras.getCharSequence("android.title");
        jo.put("apptitle", charSequenceToString(apptitle));

        jo.put("time", n.when);

        return jo;
    }

    private String charSequenceToString(CharSequence c) {
        return (c == null) ? "" : String.valueOf(c);
    }

    private String[] charSequenceArrayToStringArray(CharSequence[] c) {
        if (c == null) return new String[0];
        String[] out = new String[c.length];
        for (int i = 0; i < c.length; i++) {
            out[i] = charSequenceToString(c[i]);
        }
        return out;
    }

    private JSONArray stringArrayToJSONArray(String[] array) {
        JSONArray jsonArray = new JSONArray();
        for (String item : array) {
            jsonArray.put(item);
        }
        return jsonArray;
    }

    private boolean existsInWhitelist(StatusBarNotification notification) {
        String packageName = notification.getPackageName();
        Log.d(TAG, "Checking package: " + packageName);
        boolean exists = packagesWhitelist.contains(packageName);
        Log.d(TAG, "Package in whitelist: " + packagesWhitelist);
        Log.d(TAG, "Exists: " + exists);
        if (!exists) {
            Log.d(TAG, "Package not in whitelist, skipping");
        }
        return exists;
    }
}

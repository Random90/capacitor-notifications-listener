# capacitor-notifications-listener

Plugin for reading all android notifications in capacitor. With this plugin you can read other apps notifications and create some automations based on their contents.

It is a replacement of [https://github.com/Alone2/capacitor-notificationlistener](https://github.com/Alone2/capacitor-notificationlistener), that was archived due to lack of updates. 

This plugin works on Android 16 and adds few additional features like persistent notifications caching.
Tested on Capacitor v7.

**Note: Plugin is in active development, bugs are to be expected, especially in background processing service.** 

Since version 0.4 background service is more robust and should withstand reboots and app kills, but I've tested it only on few devices with Android 15 to 12.

## Install

```bash
npm install capacitor-notifications-listener
npx cap sync
```
Next click Android studio -> Sync project with gradle files.

To enable the listener, add this service to your `AndroidManifest.xml` inside `<application>`:
```xml
<service android:name="com.capacitor.notifications.listener.NotificationService"
    android:exported="true"
    android:label="@string/app_name"
    android:permission="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE">
    <intent-filter>
        <action android:name="android.service.notification.NotificationListenerService" />
    </intent-filter>
</service>
```
## Usage
### Basic

Import the plugin.
```typescript
import { AndroidNotification, NotificationsListener, NotificationsListenerPlugin } from 'capacitor-notifications-listener';

const systemNotificationListener: NotificationsListenerPlugin = NotificationsListener;
```

Start listening for notifications. 
```typescript
systemNotificationListener.startListening();
```

Add a listener for new notifications or the removal of notifications.
Make sure you have called ```sn.startListening()``` to be able to receive notifications.
```typescript
systemNotificationListener.addListener("notificationReceivedEvent", (notification: AndroidNotification) => {
    // logic ...
});
systemNotificationListener.addListener("notificationRemovedEvent", (notification: AndroidNotification) => {
    // logic ...
});
```

AndroidNotification Interface.
The anotomy of android notifications is explained [here](https://developer.android.com/guide/topics/ui/notifiers/notifications#Templates).
```typescript
interface AndroidNotification {
  apptitle: string;     // Title of a notifications' app
  text: string;         // Text of a notification
  textlines: string[];  // Text of a multi-line notification
  title: string;        // Title of a notification
  time: number;         // Received timestamp
  package: string;      // Package-name of a notifications' app
}
```

Check if the App is listening for notifications.
If it is not, even though ```systemNotificationListener.startListening()``` was called,
your app doesn't have sufficient permissions to observe notifications.
Call ```systemNotificationListener.requestPermission()``` to "open settings -> apps -> special app access -> notification read, reply and control" screen. User must select your app and enable this special permission manually, so make sure to instruct the user how to do it. 
```typescript
systemNotificationListener.isListening().then((isListening : boolean) => {
    if (!isListening.value)
        // show permission screen
        systemNotificationListener.requestPermission()
});
```

Open settings so that the user can authorize your app.
```typescript
systemNotificationListener.requestPermission();
```
### Apps whitelisting

To listen only to specific apps, provide array of packages names in `startListening` options object:

```Typescript
systemNotificationListener.startListening({ packagesWhitelist: ['com.example.appone', 'org.example.apptwo'] }); 
```

To replace the whitelist with the new one after initialization, use `replacePackagesWhitelist()` method.

### Notifications caching

The service will continue to receive notifications even if your WebView app was killed. If you enable caching, those notifications will be saved in Android Preferences Storage as JSON. It's not the quickest way to store data, but if your app does not process thousands of notifications, it won't be a problem. Better storage solution is in TODO. 

To enable caching, pass additional options when starting:
```TypeScript 
systemNotificationListener.startListening({ cacheNotifications: true }); 
```

Next, when your aplication resumes or starts, call this method and the plugin will send any saved notifications. Notifications will be passed the same way as the new ones are passed to your app, so you don't have to do anything else besides calling that method. After calling, cache will be cleared.
```TypeScript
systemNotificationListener.restoreCachedNotifications();
```

## API

<docgen-index>

* [`addListener('notificationRemovedEvent', ...)`](#addlistenernotificationremovedevent-)
* [`addListener('notificationReceivedEvent', ...)`](#addlistenernotificationreceivedevent-)
* [`startListening(...)`](#startlistening)
* [`restoreCachedNotifications()`](#restorecachednotifications)
* [`stopListening()`](#stoplistening)
* [`requestPermission()`](#requestpermission)
* [`isListening()`](#islistening)
* [`removeAllListeners()`](#removealllisteners)
* [`replacePackagesWhitelist(...)`](#replacepackageswhitelist)
* [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### addListener('notificationRemovedEvent', ...)

```typescript
addListener(eventName: 'notificationRemovedEvent', listenerFunc: (info: AndroidNotification) => void) => Promise<PluginListenerHandle>
```

| Param              | Type                                                                                   |
| ------------------ | -------------------------------------------------------------------------------------- |
| **`eventName`**    | <code>'notificationRemovedEvent'</code>                                                |
| **`listenerFunc`** | <code>(info: <a href="#androidnotification">AndroidNotification</a>) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

--------------------


### addListener('notificationReceivedEvent', ...)

```typescript
addListener(eventName: 'notificationReceivedEvent', listenerFunc: (info: AndroidNotification) => void) => Promise<PluginListenerHandle>
```

| Param              | Type                                                                                   |
| ------------------ | -------------------------------------------------------------------------------------- |
| **`eventName`**    | <code>'notificationReceivedEvent'</code>                                               |
| **`listenerFunc`** | <code>(info: <a href="#androidnotification">AndroidNotification</a>) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

--------------------


### startListening(...)

```typescript
startListening(options: ListenerOptions) => Promise<void>
```

| Param         | Type                                                        |
| ------------- | ----------------------------------------------------------- |
| **`options`** | <code><a href="#listeneroptions">ListenerOptions</a></code> |

--------------------


### restoreCachedNotifications()

```typescript
restoreCachedNotifications() => Promise<void>
```

Call this after attaching listeners and after starting listening. If nothing is cached, nothing will happen.

--------------------


### stopListening()

```typescript
stopListening() => Promise<void>
```

--------------------


### requestPermission()

```typescript
requestPermission() => Promise<void>
```

Navigates to special app permissions settings screen.

--------------------


### isListening()

```typescript
isListening() => Promise<{ value: boolean; }>
```

**Returns:** <code>Promise&lt;{ value: boolean; }&gt;</code>

--------------------


### removeAllListeners()

```typescript
removeAllListeners() => Promise<void>
```

--------------------


### replacePackagesWhitelist(...)

```typescript
replacePackagesWhitelist(options: { packagesWhitelist: string[] | null; }) => Promise<void>
```

Replace the current white list of packages with new one.
send null to disable whitelist.

| Param         | Type                                                  |
| ------------- | ----------------------------------------------------- |
| **`options`** | <code>{ packagesWhitelist: string[] \| null; }</code> |

--------------------


### Interfaces


#### PluginListenerHandle

| Prop         | Type                                      |
| ------------ | ----------------------------------------- |
| **`remove`** | <code>() =&gt; Promise&lt;void&gt;</code> |


#### AndroidNotification

| Prop            | Type                  |
| --------------- | --------------------- |
| **`apptitle`**  | <code>string</code>   |
| **`text`**      | <code>string</code>   |
| **`textlines`** | <code>string[]</code> |
| **`title`**     | <code>string</code>   |
| **`time`**      | <code>number</code>   |
| **`package`**   | <code>string</code>   |


#### ListenerOptions

| Prop                     | Type                          |
| ------------------------ | ----------------------------- |
| **`cacheNotifications`** | <code>boolean</code>          |
| **`packagesWhitelist`**  | <code>string[] \| null</code> |

</docgen-api>

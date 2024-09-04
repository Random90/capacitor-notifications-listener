# capacitor-notifications-listener

Read Android Apps Notifications

## Install

```bash
npm install capacitor-notifications-listener
npx cap sync
```

## API

<docgen-index>

* [`addListener('notificationRemovedEvent', ...)`](#addlistenernotificationremovedevent-)
* [`addListener('notificationReceivedEvent', ...)`](#addlistenernotificationreceivedevent-)
* [`startListening(...)`](#startlistening)
* [`stopListening()`](#stoplistening)
* [`requestPermission()`](#requestpermission)
* [`isListening()`](#islistening)
* [`removeAllListeners()`](#removealllisteners)
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


### stopListening()

```typescript
stopListening() => Promise<void>
```

--------------------


### requestPermission()

```typescript
requestPermission() => Promise<void>
```

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

| Prop                     | Type                 |
| ------------------------ | -------------------- |
| **`cacheNotifications`** | <code>boolean</code> |

</docgen-api>
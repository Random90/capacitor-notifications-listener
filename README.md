# capacitor-notifications-listener

Read Android Apps Notifications

## Install

```bash
npm install capacitor-notifications-listener
npx cap sync
```

## API

<docgen-index>

* `addListener('notificationRemovedEvent', ...)`
* `addListener('notificationReceivedEvent', ...)`
* `startListening()`
* `stopListening()`
* `requestPermission()`
* `isListening()`
* `removeAllListeners()`
* [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### addListener('notificationRemovedEvent', ...)

```typescript
addListener(eventName: 'notificationRemovedEvent', listenerFunc: (info: AndroidNotification) => void) => Promise<PluginListenerHandle>
```

| Param        | Type                                                                                |
|--------------|-------------------------------------------------------------------------------------|
| `eventName`    | <code>'notificationRemovedEvent'</code>                                             |
| `listenerFunc` | <code>(info: <a href="#androidnotification">AndroidNotification</a>) => void</code> |

**Returns:** <code>Promise<<a href="#pluginlistenerhandle">PluginListenerHandle</a>></code>

---

### addListener('notificationReceivedEvent', ...)

```typescript
addListener(eventName: 'notificationReceivedEvent', listenerFunc: (info: AndroidNotification) => void) => Promise<PluginListenerHandle>
```

| Param        | Type                                                                                |
|--------------|-------------------------------------------------------------------------------------|
| `eventName`    | <code>'notificationReceivedEvent'</code>                                            |
| `listenerFunc` | <code>(info: <a href="#androidnotification">AndroidNotification</a>) => void</code> |

**Returns:** <code>Promise<<a href="#pluginlistenerhandle">PluginListenerHandle</a>></code>

---

### startListening()

```typescript
startListening() => Promise<void>
```

---

### stopListening()

```typescript
stopListening() => Promise<void>
```

---

### requestPermission()

```typescript
requestPermission() => Promise<void>
```

---

### isListening()

```typescript
isListening() => Promise<{ value: boolean; }>
```

**Returns:** <code>Promise<{ value: boolean; }></code>

---

### removeAllListeners()

```typescript
removeAllListeners() => Promise<void>
```

---

### Interfaces

#### PluginListenerHandle

| Prop   | Type                             |
|--------|----------------------------------|
| `remove` | <code>() => Promise<void></code> |

#### AndroidNotification

| Prop      | Type                                  |
|-----------|---------------------------------------|
| `apptitle`  | <code>string</code>                   |
| `text`      | <code>string</code>                   |
| `textlines` | <code>string\[\]</code>                 |
| `title`     | <code>string</code>                   |
| `time`      | <code><a href="#date">Date</a></code> |
| `package`   | <code>string</code>                   |

#### Date

Enables basic storage and retrieval of dates and times.

| Method             | Signature                                                                                               | Description                                                                                                                             |
|--------------------|---------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------|
| **toString**           | () => string                                                                                            | Returns a string representation of a date. The format of the string depends on the locale.                                              |
| **toDateString**       | () => string                                                                                            | Returns a date as a string value.                                                                                                       |
| **toTimeString**       | () => string                                                                                            | Returns a time as a string value.                                                                                                       |
| **toLocaleString**     | () => string                                                                                            | Returns a value as a string value appropriate to the host environment's current locale.                                                 |
| **toLocaleDateString** | () => string                                                                                            | Returns a date as a string value appropriate to the host environment's current locale.                                                  |
| **toLocaleTimeString** | () => string                                                                                            | Returns a time as a string value appropriate to the host environment's current locale.                                                  |
| **valueOf**            | () => number                                                                                            | Returns the stored time value in milliseconds since midnight, January 1, 1970 UTC.                                                      |
| **getTime**            | () => number                                                                                            | Gets the time value in milliseconds.                                                                                                    |
| **getFullYear**        | () => number                                                                                            | Gets the year, using local time.                                                                                                        |
| **getUTCFullYear**     | () => number                                                                                            | Gets the year using Universal Coordinated Time (UTC).                                                                                   |
| **getMonth**           | () => number                                                                                            | Gets the month, using local time.                                                                                                       |
| **getUTCMonth**        | () => number                                                                                            | Gets the month of a <a href="#date">Date</a> object using Universal Coordinated Time (UTC).                                             |
| **getDate**            | () => number                                                                                            | Gets the day-of-the-month, using local time.                                                                                            |
| **getUTCDate**         | () => number                                                                                            | Gets the day-of-the-month, using Universal Coordinated Time (UTC).                                                                      |
| **getDay**             | () => number                                                                                            | Gets the day of the week, using local time.                                                                                             |
| **getUTCDay**          | () => number                                                                                            | Gets the day of the week using Universal Coordinated Time (UTC).                                                                        |
| **getHours**           | () => number                                                                                            | Gets the hours in a date, using local time.                                                                                             |
| **getUTCHours**        | () => number                                                                                            | Gets the hours value in a <a href="#date">Date</a> object using Universal Coordinated Time (UTC).                                       |
| **getMinutes**         | () => number                                                                                            | Gets the minutes of a <a href="#date">Date</a> object, using local time.                                                                |
| **getUTCMinutes**      | () => number                                                                                            | Gets the minutes of a <a href="#date">Date</a> object using Universal Coordinated Time (UTC).                                           |
| **getSeconds**         | () => number                                                                                            | Gets the seconds of a <a href="#date">Date</a> object, using local time.                                                                |
| **getUTCSeconds**      | () => number                                                                                            | Gets the seconds of a <a href="#date">Date</a> object using Universal Coordinated Time (UTC).                                           |
| **getMilliseconds**    | () => number                                                                                            | Gets the milliseconds of a <a href="#date">Date</a>, using local time.                                                                  |
| **getUTCMilliseconds** | () => number                                                                                            | Gets the milliseconds of a <a href="#date">Date</a> object using Universal Coordinated Time (UTC).                                      |
| **getTimezoneOffset**  | () => number                                                                                            | Gets the difference in minutes between the time on the local computer and Universal Coordinated Time (UTC).                             |
| **setTime**            | (time: number) => number                                                                                | Sets the date and time value in the <a href="#date">Date</a> object.                                                                    |
| **setMilliseconds**    | (ms: number) => number                                                                                  | Sets the milliseconds value in the <a href="#date">Date</a> object using local time.                                                    |
| **setUTCMilliseconds** | (ms: number) => number                                                                                  | Sets the milliseconds value in the <a href="#date">Date</a> object using Universal Coordinated Time (UTC).                              |
| **setSeconds**         | (sec: number, ms?: number \| undefined) => number                                                       | Sets the seconds value in the <a href="#date">Date</a> object using local time.                                                         |
| **setUTCSeconds**      | (sec: number, ms?: number \| undefined) => number                                                       | Sets the seconds value in the <a href="#date">Date</a> object using Universal Coordinated Time (UTC).                                   |
| **setMinutes**         | (min: number, sec?: number \| undefined, ms?: number | undefined) => number                             | Sets the minutes value in the <a href="#date">Date</a> object using local time.                                                         |
| **setUTCMinutes**      | (min: number, sec?: number \| undefined, ms?: number | undefined) => number                             | Sets the minutes value in the <a href="#date">Date</a> object using Universal Coordinated Time (UTC).                                   |
| **setHours**           | (hours: number, min?: number \| undefined, sec?: number | undefined, ms?: number | undefined) => number | Sets the hour value in the <a href="#date">Date</a> object using local time.                                                            |
| **setUTCHours**        | (hours: number, min?: number \| undefined, sec?: number | undefined, ms?: number | undefined) => number | Sets the hours value in the <a href="#date">Date</a> object using Universal Coordinated Time (UTC).                                     |
| **setDate**            | (date: number) => number                                                                                | Sets the numeric day-of-the-month value of the <a href="#date">Date</a> object using local time.                                        |
| **setUTCDate**         | (date: number) => number                                                                                | Sets the numeric day of the month in the <a href="#date">Date</a> object using Universal Coordinated Time (UTC).                        |
| **setMonth**           | (month: number, date?: number \| undefined) => number                                                   | Sets the month value in the <a href="#date">Date</a> object using local time.                                                           |
| **setUTCMonth**        | (month: number, date?: number \| undefined) => number                                                   | Sets the month value in the <a href="#date">Date</a> object using Universal Coordinated Time (UTC).                                     |
| **setFullYear**        | (year: number, month?: number \| undefined, date?: number | undefined) => number                        | Sets the year of the <a href="#date">Date</a> object using local time.                                                                  |
| **setUTCFullYear**     | (year: number, month?: number \| undefined, date?: number | undefined) => number                        | Sets the year value in the <a href="#date">Date</a> object using Universal Coordinated Time (UTC).                                      |
| **toUTCString**        | () => string                                                                                            | Returns a date converted to a string using Universal Coordinated Time (UTC).                                                            |
| **toISOString**        | () => string                                                                                            | Returns a date as a string value in ISO format.                                                                                         |
| **toJSON**             | (key?: any) => string                                                                                   | Used by the JSON.stringify method to enable the transformation of an object's data for JavaScript Object Notation (JSON) serialization. |

</docgen-api>
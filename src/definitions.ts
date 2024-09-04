import type { PluginListenerHandle } from '@capacitor/core';

export interface NotificationsListenerPlugin {
  addListener(
    eventName: 'notificationRemovedEvent',
    listenerFunc: (info: AndroidNotification) => void,
  ): Promise<PluginListenerHandle>;
  addListener(
    eventName: 'notificationReceivedEvent',
    listenerFunc: (info: AndroidNotification) => void,
  ): Promise<PluginListenerHandle>;
  /**
   *
   * @param cacheNotifications If true, the plugin will cache all RECEIVED notifications that and emit them when the webview is in the foreground.
   */
  startListening(options: ListenerOptions): Promise<void>;
  /**
   * Call this after attaching listeners and after starting listening. If nothing is cached, nothing will happen.
   */
  restoreCachedNotifications(): Promise<void>;
  stopListening(): Promise<void>;
  requestPermission(): Promise<void>;
  isListening(): Promise<{ value: boolean }>;
  removeAllListeners(): Promise<void>;
}

export interface ListenerOptions {
  cacheNotifications?: boolean;
}

export interface AndroidNotification {
  apptitle: string;
  text: string;
  textlines: string[];
  title: string;
  time: number; // timestamp
  package: string;
}

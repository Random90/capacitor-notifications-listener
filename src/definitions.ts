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

  startListening(options: ListenerOptions): Promise<void>;
  /**
   * Call this after attaching listeners and after starting listening. If nothing is cached, nothing will happen.
   */
  restoreCachedNotifications(): Promise<void>;
  stopListening(): Promise<void>;
  /**
   * Immediately returns { value: true } if already granted
   * Otherwise tries to open:
   * - Notification Listener detail settings for this service
   * - Generic Notification Listener settings
   * - App details settings as final fallback
   *
   * Returns { value: false } after opening settings (user still needs to toggle)
   *
   * To force open settings screen even if permission is granted, set forceOpenSettings to true.
   *
   * Use with App.addListener('resume', cb) and isPermissionGranted() to check if user granted permission after returning from settings
   */
  requestPermission(options?: { forceOpenSettings?: boolean }): Promise<void>;
  isPermissionGranted(): Promise<{ value: boolean }>;
  isListening(): Promise<{ value: boolean }>;
  removeAllListeners(): Promise<void>;
  /**
   * Replace the current white list of packages with new one.
   * send null to disable whitelist.
   */
  replacePackagesWhitelist(options: { packagesWhitelist: string[] | null }): Promise<void>;
}

export interface ListenerOptions {
  // If true, the plugin will cache all RECEIVED notifications that and emit them when the webview is in the foreground.
  cacheNotifications?: boolean;
  // listen to notifications from specific packages. Improves performance
  packagesWhitelist?: string[] | null;
  // set Capacitor Preferences storage group name. Useful for accessing data from BackgroundRunner
  storageGroupName?: string | null;
}

export interface AndroidNotification {
  apptitle: string;
  text: string;
  textlines?: string[];
  title: string;
  time: number; // timestamp
  package: string;
}

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
  startListening(): Promise<void>;
  stopListening(): Promise<void>;
  requestPermission(): Promise<void>;
  isListening(): Promise<{ value: boolean }>;
  removeAllListeners(): Promise<void>;
}

export interface AndroidNotification {
  apptitle: string;
  text: string;
  textlines: string[];
  title: string;
  time: number; // timestamp
  package: string;
}

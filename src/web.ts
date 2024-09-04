// browser can't read your notifications.
import { WebPlugin } from '@capacitor/core';

import type {
  AndroidNotification,
  NotificationsListenerPlugin,
} from './definitions';

export class NotificationsListenerWeb
  extends WebPlugin
  implements NotificationsListenerPlugin
{
  readonly errorMessage =
    'Method will never be implemented. Whose notifications you want to read?';
  attachListener(
    eventName: 'notificationRemovedEvent',
    callback: (info: AndroidNotification) => void,
  ): AndroidNotification;
  attachListener(
    eventName: 'notificationReceivedEvent',
    callback: (info: AndroidNotification) => void,
  ): AndroidNotification;
  attachListener(): AndroidNotification {
    throw new Error(this.errorMessage);
  }
  startListening(): Promise<void> {
    throw new Error(this.errorMessage);
  }
  restoreCachedNotifications(): Promise<void> {
    throw new Error(this.errorMessage);
  }
  stopListening(): Promise<void> {
    throw new Error(this.errorMessage);
  }
  requestPermission(): Promise<void> {
    throw new Error(this.errorMessage);
  }
  isListening(): Promise<{ value: boolean }> {
    throw new Error(this.errorMessage);
  }
}

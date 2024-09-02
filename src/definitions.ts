export interface NotificationsListenerPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
  attachListener(
    eventName: 'notificationRemovedEvent',
    callback: (info: AndroidNotification) => void,
  ): AndroidNotification;
  attachListener(
    eventName: 'notificationReceivedEvent',
    callback: (info: AndroidNotification) => void,
  ): AndroidNotification;
  startListening(): Promise<{ value: boolean }>;
  stopListening(): Promise<{ value: boolean }>;
  // requestPermission(): Promise<void>;
  isListening(): Promise<{ value: boolean }>;
}

export interface AndroidNotification {
  apptitle: string;
  text: string;
  textlines: string[];
  title: string;
  time: Date;
  package: string;
}

import { registerPlugin } from '@capacitor/core';

import type { NotificationsListenerPlugin } from './definitions';

const NotificationsListener = registerPlugin<NotificationsListenerPlugin>('NotificationsListener', {
  web: () => import('./web').then((m) => new m.NotificationsListenerWeb()),
});

export * from './definitions';
export { NotificationsListener };

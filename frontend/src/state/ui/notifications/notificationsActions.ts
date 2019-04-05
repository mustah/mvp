import {createStandardAction} from 'typesafe-actions';

export const seenNotifications = createStandardAction('SEEN_NOTIFICATIONS')<string>();

export const getCurrentVersion = createStandardAction('GET_CURRENT_VERSION')<string>();

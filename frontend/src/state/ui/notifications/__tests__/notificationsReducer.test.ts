import {logoutUser} from '../../../../usecases/auth/authActions';
import {getCurrentVersion, seenNotifications} from '../notificationsActions';
import {NotificationsState} from '../notificationsModels';
import {initialState, notifications} from '../notificationsReducer';

describe('notificationsReducer', () => {

  describe('keep state', () => {

    it('when user logs out', () => {
      const state: NotificationsState = {hasNotifications: true};

      expect(notifications(state, logoutUser(undefined))).toBe(state);
    });

    it('when user logs out, the version is kept too', () => {
      const state: NotificationsState = {hasNotifications: true, version: '1'};

      expect(notifications(state, logoutUser(undefined))).toBe(state);
    });

    it('when user has seen the notifications', () => {
      const state: NotificationsState = {...initialState, hasNotifications: true};

      const version = 'a';
      const expected: NotificationsState = {hasNotifications: false, version};

      expect(notifications(state, seenNotifications(version))).toEqual(expected);
    });
  });

  describe('check version on user login success', () => {

    it('should use the old version when current version and old version are the same', () => {
      const version = '1a';
      const state: NotificationsState = {hasNotifications: true, version};
      const expected: NotificationsState = {hasNotifications: false, version};

      expect(notifications(state, seenNotifications(version))).toEqual(expected);
    });

    describe('getCurrentVersion', () => {

      it('has notifications the first time user logs', () => {
        const version = '1a';
        const expected: NotificationsState = {hasNotifications: true, version};

        expect(notifications(initialState, getCurrentVersion(version))).toEqual(expected);
      });

      it('has notification when versions are not the same', () => {
        const state: NotificationsState = {hasNotifications: false, version: '1'};
        const expected: NotificationsState = {hasNotifications: true, version: '2'};

        expect(notifications(state, getCurrentVersion('2'))).toEqual(expected);
      });

      it('has notification when versions are not the same with newer version', () => {
        const state: NotificationsState = {hasNotifications: true, version: '1'};
        const expected: NotificationsState = {hasNotifications: true, version: '2'};

        expect(notifications(state, getCurrentVersion('2'))).toEqual(expected);
      });

      it('has not notifications when versions are the same', () => {
        const version = '1a';
        const state: NotificationsState = {hasNotifications: false, version};
        const expected: NotificationsState = {hasNotifications: false, version};

        expect(notifications(state, getCurrentVersion(version))).toEqual(expected);
      });
    });

    describe('integration test', () => {

      it('tests complete scenario', () => {
        let state: NotificationsState = notifications(initialState, getCurrentVersion('1'));

        expect(state).toEqual({hasNotifications: true, version: '1'});

        state = notifications(state, logoutUser(undefined));

        expect(state).toEqual({hasNotifications: true, version: '1'});

        state = notifications(state, getCurrentVersion('1'));

        expect(state).toEqual({hasNotifications: true, version: '1'});

        state = notifications(state, logoutUser(undefined));
        state = notifications(state, getCurrentVersion('2'));

        expect(state).toEqual({hasNotifications: true, version: '2'});
      });
    });

  });

});

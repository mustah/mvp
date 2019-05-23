import {toLocation} from '../../../__tests__/testDataFactory';
import {routes} from '../../../app/routes';
import {locationChange} from '../../../state/location/locationActions';
import {ErrorResponse} from '../../../types/Types';
import {failureTheme, requestTheme, successTheme} from '../themeActions';
import {Colors, ThemeState} from '../themeModels';
import {initialState, theme} from '../themeReducer';

describe('themeReducer', () => {

  describe('change colors', () => {

    it('changes primary color', () => {
      const {color: {secondary}} = initialState;
      const state: ThemeState = theme(initialState, successTheme({primary: 'red', secondary}));

      const expected: ThemeState = {
        ...initialState,
        color: {...initialState.color, primary: 'red'},
        isSuccessfullyFetched: true
      };
      expect(state).toEqual(expected);
    });

    it('changes secondary color', () => {
      const {color: {primary}} = initialState;

      const state: ThemeState = theme(initialState, successTheme({primary, secondary: 'blue'}));

      const expected: ThemeState = {
        ...initialState,
        color: {...initialState.color, secondary: 'blue'},
        isSuccessfullyFetched: true
      };
      expect(state).toEqual(expected);
    });

    it('changes both primary and secondary colors', () => {
      const state: ThemeState = theme(initialState, successTheme({primary: 'red', secondary: 'blue'}));

      const expected: ThemeState = {
        ...initialState,
        color: {primary: 'red', secondary: 'blue'},
        isSuccessfullyFetched: true
      };
      expect(state).toEqual(expected);
    });
  });

  describe('do fetch theme actions', () => {

    it('can request theme', () => {
      const state: ThemeState = theme(initialState, requestTheme());

      const expected: ThemeState = {...initialState, isFetching: true};
      expect(state).toEqual(expected);
    });

    it('can fail when requesting theme', () => {
      const message: ErrorResponse = {message: 'failed'};

      const state: ThemeState = theme(initialState, failureTheme(message));

      const expected: ThemeState = {...initialState, error: {...message}};
      expect(state).toEqual(expected);
    });

    it('can fail after a request has been made', () => {
      const message: ErrorResponse = {message: 'failed'};

      const state: ThemeState = theme({...initialState, isFetching: true}, failureTheme(message));

      const expected: ThemeState = {...initialState, isFetching: false, error: {...message}};
      expect(state).toEqual(expected);
    });

    it('can fetch handle colors successfully', () => {
      const color: Colors = {primary: 'red', secondary: 'blue'};

      const state: ThemeState = theme(initialState, successTheme(color));

      const expected: ThemeState = {color, isFetching: false, isSuccessfullyFetched: true};
      expect(state).toEqual(expected);
    });

  });

  describe('location change', () => {

    it('will reset state when location is organisation list', () => {
      let state: ThemeState = {
        color: {primary: 'red', secondary: 'blue'},
        isFetching: false,
        isSuccessfullyFetched: true,
      };

      state = theme(state, locationChange(toLocation(routes.adminOrganisationsModify)));

      const expected: ThemeState = {
        color: {primary: 'red', secondary: 'blue'},
        isFetching: false,
        isSuccessfullyFetched: false
      };
      expect(state).toEqual(expected);
    });

    it('will not reset state when location is organisation list', () => {
      const state: ThemeState = {
        ...initialState,
        isSuccessfullyFetched: true,
        color: {
          primary: 'red',
          secondary: 'blue'
        }
      };

      const newState: ThemeState = theme(state, locationChange(toLocation(routes.adminOrganisationsAdd)));

      expect(state).toBe(newState);
    });
  });
});

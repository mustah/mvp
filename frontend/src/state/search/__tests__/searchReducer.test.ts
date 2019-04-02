import {Location} from 'history';
import {routes} from '../../../app/routes';
import {logoutUser} from '../../../usecases/auth/authActions';
import {locationChange} from '../../location/locationActions';
import {search as searchAction} from '../searchActions';
import {makeMeterQuery, QueryParameter} from '../searchModels';
import {initialState, search, SearchState} from '../searchReducer';

describe('searchReducer', () => {

  describe('meter search query', () => {

    it('has no meter search query', () => {
      const payload: QueryParameter = makeMeterQuery();
      const state: SearchState = search(
        initialState,
        searchAction(payload),
      );

      expect(state).toEqual({...initialState, validation: {}});
    });

    it('has meter search query', () => {
      const payload: QueryParameter = makeMeterQuery('bro');
      const state: SearchState = search(initialState, searchAction(payload));

      expect(state).toEqual({...initialState, validation: {query: 'bro'}});
    });

    it('replaces previous meter search query', () => {
      let state: SearchState = search(initialState, searchAction(makeMeterQuery('bro')));

      expect(state).toEqual({...initialState, validation: {query: 'bro'}});

      state = search(initialState, searchAction(makeMeterQuery('hop')));
      expect(state).toEqual({...initialState, validation: {query: 'hop'}});
    });
  });

  describe('reset query', () => {

    it('reset validation query when location changes to selection page', () => {
      let state: SearchState = {
        ...initialState,
        ...makeMeterQuery('kam')
      };

      const payload: Partial<Location> = {pathname: routes.selection};
      state = search(state, locationChange(payload as Location));

      expect(state).toEqual(initialState);
    });

    it('reset validation query when location changes to dashboard page', () => {
      let state: SearchState = {
        ...initialState,
        ...makeMeterQuery('kam')
      };

      const payload: Partial<Location> = {pathname: routes.dashboard};
      state = search(state, locationChange(payload as Location));

      expect(state).toEqual(initialState);
    });

    it('reset validation query when location changes to meter page', () => {
      const state: SearchState = {
        ...initialState,
        ...makeMeterQuery('kam')
      };

      const payload: Partial<Location> = {pathname: routes.collection};
      const newState = search(state, locationChange(payload as Location));

      expect(newState).toEqual(initialState);
    });
  });

  describe('logout user', () => {

    it('resets state to initial state', () => {
      let state: SearchState = {
        ...initialState,
        ...makeMeterQuery('kungsbacka'),
      };

      state = search(state, logoutUser(undefined));

      expect(state).toEqual(initialState);
    });
  });
});

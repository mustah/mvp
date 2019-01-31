import {LOCATION_CHANGE} from 'react-router-redux';
import {routes} from '../../../app/routes';
import {LOGOUT_USER} from '../../auth/authActions';
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
        validation: {query: 'bro'},
      };

      state = search(state, {type: LOCATION_CHANGE, payload: {pathname: routes.selection}});

      expect(state).toEqual({
        ...initialState,
        validation: {},
      });
    });

    it('does not reset validation query when location does not changes to selection page', () => {
      const state: SearchState = {
        ...initialState,
        validation: {query: 'bro'},
      };

      const newState = search(state, {type: LOCATION_CHANGE, payload: {pathname: routes.collection}});

      expect(newState).toBe(state);
    });
  });

  describe('logout user', () => {

    it('resets state to initial state', () => {
      let state: SearchState = {
        ...initialState,
        ...makeMeterQuery('kungsbacka'),
      };

      state = search(state, {type: LOGOUT_USER});

      expect(state).toEqual(initialState);
    });
  });
});

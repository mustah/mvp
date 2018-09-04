import {LOCATION_CHANGE} from 'react-router-redux';
import {routes} from '../../../app/routes';
import {LOGOUT_USER} from '../../auth/authActions';
import {search as searchAction} from '../searchActions';
import {collectionQuery, QueryParameter, selectionTreeQuery, validationQuery} from '../searchModels';
import {initialState, search, SearchState} from '../searchReducer';

describe('searchReducer', () => {

  describe('meter search query', () => {

    it('has no meter search query', () => {
      const payload: QueryParameter = validationQuery();
      const state: SearchState = search(
        initialState,
        searchAction(payload),
      );

      expect(state).toEqual({...initialState, validation: {}});
    });

    it('has meter search query', () => {
      const payload: QueryParameter = validationQuery('bro');
      const state: SearchState = search(initialState, searchAction(payload));

      expect(state).toEqual({...initialState, validation: {query: 'bro'}});
    });

    it('replaces previous meter search query', () => {
      let state: SearchState = search(initialState, searchAction(validationQuery('bro')));

      expect(state).toEqual({...initialState, validation: {query: 'bro'}});

      state = search(initialState, searchAction(validationQuery('hop')));
      expect(state).toEqual({...initialState, validation: {query: 'hop'}});
    });
  });

  describe('gateway search query', () => {

    it('has no gateway search query', () => {
      const payload: QueryParameter = collectionQuery();
      const state: SearchState = search(
        initialState,
        searchAction(payload),
      );

      expect(state).toEqual({...initialState, collection: {}});
    });

    it('has gateway search query', () => {
      const payload: QueryParameter = collectionQuery('bro');
      const state: SearchState = search(initialState, searchAction(payload));

      expect(state).toEqual({...initialState, collection: {query: 'bro'}});
    });

    it('replaces previous gateway search query', () => {
      let state: SearchState = search(initialState, searchAction(collectionQuery('bro')));

      expect(state).toEqual({...initialState, collection: {query: 'bro'}});

      state = search(initialState, searchAction(collectionQuery('hop')));
      expect(state).toEqual({...initialState, collection: {query: 'hop'}});
    });
  });

  describe('selectionTreeSearch', () => {

    it('defaults to empty', () => {
      const payload: QueryParameter = selectionTreeQuery();
      const state: SearchState = search(
        initialState,
        searchAction(payload),
      );

      const expected: SearchState = {...initialState, selectionTree: {}};

      expect(state).toEqual(expected);
    });

    it('can search in selection tree', () => {
      const payload: QueryParameter = selectionTreeQuery('bro');
      const state: SearchState = search(initialState, searchAction(payload));
      const expected: SearchState = {...initialState, selectionTree: {query: 'bro'}};

      expect(state).toEqual(expected);
    });

    it('replaces previous selection tree search query', () => {
      const firstState: SearchState = search(initialState, searchAction(selectionTreeQuery('bro')));
      const firstExpected: SearchState = {...initialState, selectionTree: {query: 'bro'}};

      expect(firstState).toEqual(firstExpected);

      const secondState: SearchState = search(initialState, searchAction(selectionTreeQuery('hop')));
      const secondExpected: SearchState = {...initialState, selectionTree: {query: 'hop'}};
      expect(secondState).toEqual(secondExpected);
    });
  });

  describe('gateway and meter search queries', () => {

    it('replaces only gateway query', () => {
      let state: SearchState = search(initialState, searchAction(validationQuery('bro')));

      state = search(state, searchAction(collectionQuery('stop')));

      expect(state).toEqual({
        ...initialState,
        collection: {query: 'stop'},
        validation: {query: 'bro'},
      });
    });
  });

  describe('reset query', () => {

    it('reset validation query when location changes to selection page', () => {
      let state: SearchState = {
        ...initialState,
        collection: {query: 'stop'},
        validation: {query: 'bro'},
      };

      state = search(state, {type: LOCATION_CHANGE, payload: {pathname: routes.selection}});

      expect(state).toEqual({
        ...initialState,
        collection: {query: 'stop'},
        validation: {},
      });
    });

    it('does not reset validation query when location does not changes to selection page', () => {
      const state: SearchState = {
        ...initialState,
        collection: {query: 'stop'},
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
        ...validationQuery('kungsbacka'),
        ...collectionQuery('CMi'),
      };

      state = search(state, {type: LOGOUT_USER});

      expect(state).toEqual(initialState);
    });
  });
});

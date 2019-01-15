import {LOCATION_CHANGE} from 'react-router-redux';
import {routes} from '../../../app/routes';
import {LOGOUT_USER} from '../../auth/authActions';
import {search as searchAction} from '../searchActions';
import {gatewayQuery, QueryParameter, selectionTreeQuery, meterQuery} from '../searchModels';
import {initialState, search, SearchState} from '../searchReducer';

describe('searchReducer', () => {

  describe('meter search query', () => {

    it('has no meter search query', () => {
      const payload: QueryParameter = meterQuery();
      const state: SearchState = search(
        initialState,
        searchAction(payload),
      );

      expect(state).toEqual({...initialState, validation: {}});
    });

    it('has meter search query', () => {
      const payload: QueryParameter = meterQuery('bro');
      const state: SearchState = search(initialState, searchAction(payload));

      expect(state).toEqual({...initialState, validation: {query: 'bro'}});
    });

    it('replaces previous meter search query', () => {
      let state: SearchState = search(initialState, searchAction(meterQuery('bro')));

      expect(state).toEqual({...initialState, validation: {query: 'bro'}});

      state = search(initialState, searchAction(meterQuery('hop')));
      expect(state).toEqual({...initialState, validation: {query: 'hop'}});
    });
  });

  describe('gateway search query', () => {

    it('has no gateway search query', () => {
      const payload: QueryParameter = gatewayQuery();
      const state: SearchState = search(
        initialState,
        searchAction(payload),
      );

      expect(state).toEqual({...initialState, collection: {}});
    });

    it('has gateway search query', () => {
      const payload: QueryParameter = gatewayQuery('bro');
      const state: SearchState = search(initialState, searchAction(payload));

      expect(state).toEqual({...initialState, collection: {query: 'bro'}});
    });

    it('replaces previous gateway search query', () => {
      let state: SearchState = search(initialState, searchAction(gatewayQuery('bro')));

      expect(state).toEqual({...initialState, collection: {query: 'bro'}});

      state = search(initialState, searchAction(gatewayQuery('hop')));
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

    it('keeps the validation and collection queries when searching the selection tree', () => {
      const state: SearchState = search(
        {...initialState, validation: {query: 'asdf'}, collection: {query: 'fdsa'}},
        searchAction(selectionTreeQuery('bro')),
      );
      const expected: SearchState = {
        ...initialState,
        validation: {query: 'asdf'},
        collection: {query: 'fdsa'},
        selectionTree: {query: 'bro'},
      };

      expect(state).toEqual(expected);
    });

  });

  describe('gateway and meter search queries', () => {

    it('replaces only gateway query', () => {
      let state: SearchState = search(initialState, searchAction(meterQuery('bro')));

      state = search(state, searchAction(gatewayQuery('stop')));

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
        ...meterQuery('kungsbacka'),
        ...gatewayQuery('CMi'),
      };

      state = search(state, {type: LOGOUT_USER});

      expect(state).toEqual(initialState);
    });
  });
});

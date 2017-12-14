import {changeTabCollection, changeTabValidation} from '../tabsActions';
import {TabName} from '../tabsModels';
import {initialState, tabs} from '../tabsReducer';

describe('tabsReducer', () => {

  describe('changeTab', () => {

    it('returns previous state when action type is not matched', () => {
      const state = tabs(initialState, changeTabValidation(TabName.map));

      expect(tabs(state, {type: 'unknown'})).toBe(state);
    });

    it('changes tab to map in validation use case', () => {
      const state = tabs(initialState, changeTabValidation(TabName.map));

      expect(state).toEqual({
        ...initialState,
        validation: {selectedTab: TabName.map},
      });
    });

    it('changes tabs in validation use case', () => {
      let state = tabs(initialState, changeTabValidation(TabName.map));

      expect(state).toEqual({
        ...initialState,
        validation: {selectedTab: TabName.map},
      });

      state = tabs(state, changeTabValidation(TabName.list));

      expect(state).toEqual({
        ...initialState,
        validation: {selectedTab: TabName.list},
      });
    });

    it('changes tab to list in collection use case', () => {
      const state = tabs(initialState, changeTabCollection(TabName.list));

      expect(state).toEqual({
        ...initialState,
        collection: {selectedTab: TabName.list},
      });
    });

    it('changes tabs in collection use case', () => {
      let state = tabs(initialState, changeTabCollection(TabName.graph));

      expect(state).toEqual({
        ...initialState,
        collection: {selectedTab: TabName.graph},
      });

      state = tabs(state, changeTabCollection(TabName.list));

      expect(state).toEqual({
        ...initialState,
        collection: {selectedTab: TabName.list},
      });
    });

  });

});

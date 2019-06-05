import {changeTabGateway, changeTabMeter, changeTabReport, unknownAction} from '../tabsActions';
import {TabName, TabsState} from '../tabsModels';
import {initialState, tabs} from '../tabsReducer';

describe('tabsReducer', () => {

  describe('changeTab', () => {

    it('changes tab to map in validation use case', () => {
      const state: TabsState = tabs(initialState, changeTabMeter(TabName.map));

      const expected: TabsState = {
        ...initialState,
        validation: {selectedTab: TabName.map},
      };
      expect(state).toEqual(expected);
    });

    it('changes tabs in validation use case', () => {
      let state: TabsState = tabs(initialState, changeTabMeter(TabName.map));

      const expected: TabsState = {
        ...initialState,
        validation: {selectedTab: TabName.map},
      };
      expect(state).toEqual(expected);

      state = tabs(state, changeTabMeter(TabName.list));

      const expected2: TabsState = {
        ...initialState,
        validation: {selectedTab: TabName.list},
      };
      expect(state).toEqual(expected2);
    });

    it('changes tab to list in collection use case', () => {
      const state = tabs(initialState, changeTabGateway(TabName.list));

      expect(state).toEqual({
        ...initialState,
        collection: {selectedTab: TabName.list},
      });
    });

    it('changes tabs in collection use case', () => {
      let state: TabsState = tabs(initialState, changeTabGateway(TabName.graph));

      const expected: TabsState = {
        ...initialState,
        collection: {selectedTab: TabName.graph},
      };
      expect(state).toEqual(expected);

      state = tabs(state, changeTabGateway(TabName.list));

      const expected2: TabsState = {
        ...initialState,
        collection: {selectedTab: TabName.list},
      };
      expect(state).toEqual(expected2);
    });

  });

  describe('initial state for report view', () => {

    it('is graph tab', () => {
      const state: TabsState = tabs(initialState, unknownAction());

      const expected: TabsState = {
        ...initialState,
        report: {selectedTab: TabName.graph},
      };
      expect(state).toEqual(expected);
    });

    it('changes tab to list in report view', () => {
      const state: TabsState = tabs(initialState, changeTabReport(TabName.list));

      const expected: TabsState = {
        ...initialState,
        report: {selectedTab: TabName.list},
      };
      expect(state).toEqual(expected);
    });

  });

});

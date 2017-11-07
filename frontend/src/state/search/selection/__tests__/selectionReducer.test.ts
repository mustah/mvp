import {saveSelection, selectSavedSelectionAction} from '../selectionActions';
import {SelectionState} from '../selectionModels';
import {initialState, saved, selection} from '../selectionReducer';

describe('selectionReducer', () => {

  describe('save current selection', () => {

    it('has initial state', () => {
      expect(saved([], {type: 'unknown'})).toEqual([]);
    });

    it('saves selection to empty saved list', () => {
      const payload: SelectionState = {
        id: 1,
        name: 'saved parameters',
        selected: {
          cities: [1, 2],
        },
      };
      expect(saved([], saveSelection(payload))).toEqual([{...payload}]);
    });

    it('saves selection to already existing saved selections', () => {
      const payload: SelectionState = {
        id: 1,
        name: 'saved parameters',
        selected: {
          cities: [1, 2],
        },
      };
      const newPayload: SelectionState = {
        id: 5,
        name: 'something else',
        selected: {
          cities: [1, 2],
          addresses: [1, 2, 3],
        },
      };

      let state = saved([], saveSelection(payload));
      state = saved(state, saveSelection(newPayload));

      expect(state).toEqual([payload, newPayload]);
    });
  });

  describe('select saved selection', () => {

    it('replaces current selection', () => {
      const payload: SelectionState = {
        id: 5,
        name: 'something else',
        selected: {
          cities: [1, 2],
          addresses: [1, 2, 3],
        },
      };

      const state = selection(initialState, selectSavedSelectionAction(payload));

      expect(state).toEqual({...payload});
    });
  });

});

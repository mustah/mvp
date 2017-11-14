import {idGenerator} from '../../../../services/idGenerator';
import {saveSelectionAction, selectSavedSelectionAction, updateSelectionAction} from '../selectionActions';
import {SelectionState} from '../selectionModels';
import {initialState, saved, selection} from '../selectionReducer';

describe('selectionReducer', () => {

  const mockPayload: SelectionState = {
    id: 5,
    name: 'something else',
    isChanged: false,
    selected: {
      cities: [1, 2],
      addresses: [1, 2, 3],
    },
  };

  describe('save current selection', () => {

    it('has initial state', () => {
      expect(saved([], {type: 'unknown'})).toEqual([]);
    });

    it('saves selection to empty saved list', () => {
      const payload: SelectionState = {
        id: 1,
        name: 'saved parameters',
        isChanged: false,
        selected: {
          cities: [1, 2],
        },
      };
      expect(saved([], saveSelectionAction(payload))).toEqual([{...payload}]);
    });

    it('saves selection to already existing saved selections', () => {
      const payload: SelectionState = {
        id: 1,
        name: 'saved parameters',
        isChanged: false,
        selected: {
          cities: [1, 2],
        },
      };
      const newPayload: SelectionState = {...mockPayload};

      let state = saved([], saveSelectionAction(payload));
      state = saved(state, saveSelectionAction(newPayload));

      expect(state).toEqual([newPayload, payload]);
    });
  });

  describe('select saved selection', () => {

    it('replaces current selection', () => {
      const state = selection(initialState, selectSavedSelectionAction(mockPayload));

      expect(state).toEqual({...mockPayload, isChanged: false});
    });
  });

  describe('saved selections', () => {

    it('saves new selection', () => {
      const state = saved([], saveSelectionAction(mockPayload));

      expect(state).toEqual([{...mockPayload}]);
    });

    it('update name of the selection', () => {
      let state = saved([], saveSelectionAction(mockPayload));
      state = saved(state, updateSelectionAction({...mockPayload, name: 'test'}));

      expect(state).toEqual([{...mockPayload, name: 'test'}]);
    });

    it('returns same state reference when saved state with id does not exist', () => {
      let state = saved([], saveSelectionAction(mockPayload));
      state = saved(state, updateSelectionAction({...mockPayload, id: idGenerator.uuid(), name: 'test'}));

      expect(state).toBe(state);
    });
  });

});

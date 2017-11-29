import {selectSavedSelectionAction} from '../selectionActions';
import {SelectionState} from '../selectionModels';
import {initialState, selection} from '../selectionReducer';

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

  describe('select saved selections', () => {

    it('replaces current selection', () => {
      const state = selection(initialState, selectSavedSelectionAction(mockPayload));

      expect(state).toEqual({...mockPayload, isChanged: false});
    });
  });

});

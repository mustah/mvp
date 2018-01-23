import {Period} from '../../../../components/dates/dateModels';
import {saved} from '../saveReducer';
import {saveSelectionAction, updateSelectionAction} from '../selectionActions';
import {SelectionState} from '../selectionModels';

describe('selectionReducer', () => {

  const mockPayload: SelectionState = {
    id: 5,
    name: 'something else',
    isChanged: false,
    selected: {
      cities: [1, 2],
      addresses: [1, 2, 3],
      period: Period.latest,
    },
  };

  describe('save current selection', () => {

    const payload: SelectionState = {
      id: 1,
      name: 'saved parameters',
      isChanged: false,
      selected: {
        cities: [1, 2],
        period: Period.latest,
      },
    };

    it('has initial state', () => {
      expect(saved([], {type: 'unknown', payload: {...payload}})).toEqual([]);
    });

    it('saves selection to empty saved list', () => {
      expect(saved([], saveSelectionAction(payload))).toEqual([{...payload}]);
    });

    it('saves selection to already existing saved selections', () => {
      const newPayload: SelectionState = {...mockPayload};

      let state = saved([], saveSelectionAction(payload));
      state = saved(state, saveSelectionAction(newPayload));

      expect(state).toEqual([newPayload, payload]);
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
  });

});

import {Period} from '../../../../components/dates/dateModels';
import {saved} from '../saveReducer';
import {SAVE_SELECTION, SET_CURRENT_SELECTION} from '../selectionActions';
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
      expect(saved(undefined, {type: 'unknown', payload: {...payload}})).toEqual([]);
    });

    it('saves selection to empty saved list', () => {
      expect(saved([], {type: SAVE_SELECTION, payload})).toEqual([{...payload}]);
    });

    it('saves selection to already existing saved selections', () => {
      const newPayload: SelectionState = {...mockPayload};

      let state = saved([], {type: SAVE_SELECTION, payload});
      state = saved(state, {type: SAVE_SELECTION, payload: newPayload});

      expect(state).toEqual([newPayload, payload]);
    });
  });

  describe('saved selections', () => {

    it('saves new selection', () => {
      const state = saved([], {type: SAVE_SELECTION, payload: mockPayload});

      expect(state).toEqual([{...mockPayload}]);
    });

    it('update name of the selection', () => {
      let state = saved([], {type: SAVE_SELECTION, payload: mockPayload});
      state = saved(state, {type: SET_CURRENT_SELECTION, payload: {...mockPayload, name: 'test'}});

      expect(state).toEqual([{...mockPayload, name: 'test'}]);
    });
  });

});

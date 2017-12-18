import {Action, uuid} from '../../../types/Types';
import {SET_SELECTED_ENTRIES} from '../reportActions';
import {report} from '../reportReducer';

describe('reportReducer', () => {
  it('makes sure the selectedListItems is set to payload', () => {
    const state = {selectedListItems: [4, 5]};
    const payload: uuid[] = [1, 2, 3];
    const action: Action<uuid[]> =  {type: SET_SELECTED_ENTRIES, payload};

    expect(report(state, action)).toEqual({
      ...state,
      selectedListItems: payload,
    });
  });
});

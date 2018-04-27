import {mockSelectionAction} from '../../../__tests__/testActions';
import {Action, uuid} from '../../../types/Types';
import {SET_SELECTED_ENTRIES} from '../reportActions';
import {ReportState} from '../reportModels';
import {initialState, report} from '../reportReducer';

describe('reportReducer', () => {

  it('makes sure the selectedListItems is set to payload', () => {
    const state: ReportState = {selectedListItems: [4, 5]};
    const payload: uuid[] = [1, 2, 3];
    const action: Action<uuid[]> = {type: SET_SELECTED_ENTRIES, payload};

    const expectedState: ReportState = {
      ...state,
      selectedListItems: payload,
    };
    expect(report(state, action)).toEqual(expectedState);
  });

  it('deselects all selected list items when the main selection is changed', () => {
    const state: ReportState = {selectedListItems: [1, 2, 3]};
    const selectionRelatedAction = {...mockSelectionAction};

    const expectedState: ReportState = {
      ...initialState,
    };
    expect(report(state, selectionRelatedAction)).toEqual(expectedState);
  });

});

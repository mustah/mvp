import {mockSelectionAction} from '../../../__tests__/testActions';
import {Period} from '../../../components/dates/dateModels';
import {Medium, Quantity} from '../../../state/ui/graph/measurement/measurementModels';
import {selectPeriod} from '../../../state/user-selection/userSelectionActions';
import {uuid} from '../../../types/Types';
import {logoutUser} from '../../auth/authActions';
import {SelectedEntriesPayload, setSelectedEntries} from '../reportActions';
import {ReportState} from '../reportModels';
import {initialState, report} from '../reportReducer';

describe('reportReducer', () => {

  it('makes sure the selectedListItems is set to payload', () => {
    const state: ReportState = {selectedListItems: [4, 5]};
    const ids: uuid[] = [1];
    const payload: SelectedEntriesPayload = {
      ids,
      quantitiesToSelect: [Quantity.energy],
      indicatorsToSelect: [Medium.districtHeating],
    };
    const action = setSelectedEntries(payload);
    const {selectedListItems} = report(state, action);

    expect(selectedListItems).toContain(1);
    expect(selectedListItems).not.toContain(4);
  });

  it('deselects all selected list items when the main selection is changed', () => {
    const state: ReportState = {selectedListItems: [1, 2, 3]};
    const selectionRelatedAction = {...mockSelectionAction};

    const {selectedListItems} = report(state, selectionRelatedAction);
    expect(selectedListItems).toHaveLength(0);
  });

  describe('logout user', () => {

    it('resets state to initial state', () => {
      const state: ReportState = report({selectedListItems: [1, 2, 3]}, logoutUser(undefined));

      expect(state).toEqual({...initialState});
    });
  });

  describe('change period', () => {

    it('should not clear selected list items', () => {
      const payload: SelectedEntriesPayload = {
        ids: [1, 2, 3],
        quantitiesToSelect: [],
        indicatorsToSelect: [],
      };
      const state: ReportState = report(initialState, setSelectedEntries(payload));

      const expected: ReportState = {selectedListItems: payload.ids};
      expect(state).toEqual(expected);

      const newState: ReportState = report(state, selectPeriod(Period.currentMonth));
      expect(newState).toBe(state);
    });

  });

});

import {mockSelectionAction} from '../../../__tests__/testActions';
import {Medium} from '../../../components/indicators/indicatorWidgetModels';
import {Quantity} from '../../../state/ui/graph/measurement/measurementModels';
import {uuid} from '../../../types/Types';
import {LOGOUT_USER} from '../../auth/authActions';
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
      let state: ReportState = {selectedListItems: [1, 2, 3]};
      state = report(state, {type: LOGOUT_USER});

      expect(state).toEqual({...initialState});
    });
  });

});

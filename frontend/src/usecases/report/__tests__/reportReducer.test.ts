import {mockSelectionAction} from '../../../__tests__/testActions';
import {DateRange, Period} from '../../../components/dates/dateModels';
import {momentFrom} from '../../../helpers/dateHelpers';
import {Medium, Quantity} from '../../../state/ui/graph/measurement/measurementModels';
import {selectPeriod, setCustomDateRange} from '../../../state/user-selection/userSelectionActions';
import {uuid} from '../../../types/Types';
import {logoutUser} from '../../auth/authActions';
import {setSelectedEntries, toggleLine} from '../reportActions';
import {ReportState, SelectedReportEntriesPayload} from '../reportModels';
import {initialState, report} from '../reportReducer';

describe('reportReducer', () => {

  it('makes sure the selectedListItems is set to payload', () => {
    const state: ReportState = {...initialState, selectedListItems: [4, 5]};
    const ids: uuid[] = [1];
    const payload: SelectedReportEntriesPayload = {
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
    const state: ReportState = {...initialState, selectedListItems: [1, 2, 3]};
    const selectionRelatedAction = {...mockSelectionAction};

    const {selectedListItems} = report(state, selectionRelatedAction);
    expect(selectedListItems).toHaveLength(0);
  });

  describe('logout user', () => {

    it('resets state to initial state', () => {
      const state: ReportState = report({...initialState, selectedListItems: [1, 2, 3]}, logoutUser(undefined));

      expect(state).toEqual({...initialState});
    });
  });

  describe('change period', () => {

    it('should not clear selected list items', () => {
      const payload: SelectedReportEntriesPayload = {
        ids: [1, 2, 3],
        quantitiesToSelect: [],
        indicatorsToSelect: [],
      };
      const state: ReportState = report(initialState, setSelectedEntries(payload));

      const expected: ReportState = {...initialState, selectedListItems: payload.ids};
      expect(state).toEqual(expected);

      const newState: ReportState = report(state, selectPeriod(Period.currentMonth));
      expect(newState).toBe(state);
    });

  });

  describe('change custom date range', () => {

    it('should not clear selected list items', () => {
      const start: Date = momentFrom('2018-12-09').toDate();
      const end: Date = momentFrom('2018-12-24').toDate();
      const dateRange: DateRange = {start, end};

      const payload: SelectedReportEntriesPayload = {
        ids: [1, 2, 3],
        quantitiesToSelect: [],
        indicatorsToSelect: [],
      };
      const state: ReportState = report(initialState, setSelectedEntries(payload));

      const expected: ReportState = {selectedListItems: payload.ids, hiddenLines: []};
      expect(state).toEqual(expected);

      const newState: ReportState = report(state, setCustomDateRange(dateRange));
      expect(newState).toBe(state);
    });

  });

  describe('toggleLine', () => {

    it('should add hide legend item id', () => {
      const state: ReportState = report(initialState, toggleLine(2));

      const expected: ReportState = {...initialState, hiddenLines: [2]};
      expect(state).toEqual(expected);
    });

    it('should remove existing hidden legend item id ', () => {
      const state: ReportState = report({...initialState, hiddenLines: [2]}, toggleLine(2));

      const expected: ReportState = {...initialState, hiddenLines: []};
      expect(state).toEqual(expected);
    });

    it('appends to hidden legend items', () => {
      const state: ReportState = report({...initialState, hiddenLines: [2]}, toggleLine(3));

      const expected: ReportState = {...initialState, hiddenLines: [2, 3]};
      expect(state).toEqual(expected);
    });
  });

});

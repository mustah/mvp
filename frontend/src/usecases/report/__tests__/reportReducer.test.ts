import {mockSelectionAction} from '../../../__tests__/testActions';
import {DateRange, Period, TemporalResolution} from '../../../components/dates/dateModels';
import {momentFrom} from '../../../helpers/dateHelpers';
import {Medium, Quantity} from '../../../state/ui/graph/measurement/measurementModels';
import {selectPeriod, setCustomDateRange} from '../../../state/user-selection/userSelectionActions';
import {uuid} from '../../../types/Types';
import {logoutUser} from '../../auth/authActions';
import {
  hideAllLines,
  removeSelectedListItems,
  selectResolution,
  setSelectedEntries,
  toggleLine
} from '../reportActions';
import {ReportState, SelectedReportEntries} from '../reportModels';
import {initialState, report} from '../reportReducer';

describe('reportReducer', () => {

  it('makes sure the selectedListItems is set to payload', () => {
    const state: ReportState = {...initialState, selectedListItems: [4, 5]};
    const ids: uuid[] = [1];
    const payload: SelectedReportEntries = {
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
      const payload: SelectedReportEntries = {
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

      const payload: SelectedReportEntries = {
        ids: [1, 2, 3],
        quantitiesToSelect: [],
        indicatorsToSelect: [],
      };
      const state: ReportState = report(initialState, setSelectedEntries(payload));

      const expected: ReportState = {
        selectedListItems: payload.ids,
        hiddenLines: [],
        resolution: TemporalResolution.hour
      };
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

  describe('selectResolution', () => {

    it('can select hourly resolution', () => {
      const payload = TemporalResolution.hour;

      const state: ReportState = report(initialState, selectResolution(payload));

      const expected: ReportState = {...initialState, resolution: payload};
      expect(state).toEqual(expected);
    });

    it('changes resolution', () => {
      const payload = TemporalResolution.hour;

      let state: ReportState = report(initialState, selectResolution(payload));

      let expected: ReportState = {...initialState, resolution: payload};
      expect(state).toEqual(expected);

      state = report(initialState, selectResolution(TemporalResolution.month));

      expected = {...initialState, resolution: TemporalResolution.month};
      expect(state).toEqual(expected);
    });
  });

  describe('hideAllLines', () => {

    it('hides empty list of lines', () => {
      const state: ReportState = report(initialState, hideAllLines());
      expect(state).toEqual(initialState);
    });

    it('hides single line', () => {
      const startState: ReportState = {
        ...initialState,
        selectedListItems: [13],
        hiddenLines: []
      };

      const state: ReportState = report(startState, hideAllLines());

      expect(state).toEqual({...startState, hiddenLines: [13]});
    });

    it('hides all lines', () => {
      const startState: ReportState = {
        ...initialState,
        selectedListItems: [1, 3, 5],
        hiddenLines: []
      };

      const state: ReportState = report(startState, hideAllLines());

      expect(state).toEqual({...startState, hiddenLines: [1, 3, 5]});
    });
  });

  describe('removeSelectedListItems', () => {

    it('can handle empty selected list items', () => {
      const nextState: ReportState = report(initialState, removeSelectedListItems());
      expect(nextState).toEqual(initialState);
    });

    it('removes single selected list item', () => {
      const state: ReportState = {
        ...initialState,
        selectedListItems: [13],
      };

      const nextState: ReportState = report(state, removeSelectedListItems());

      const expected: ReportState = {...state, selectedListItems: []};
      expect(nextState).toEqual(expected);
    });

    it('removes all selected list items', () => {
      const state: ReportState = {
        ...initialState,
        selectedListItems: [1, 3, 5],
      };

      const nextState: ReportState = report(state, removeSelectedListItems());

      const expected: ReportState = {...state, selectedListItems: []};
      expect(nextState).toEqual(expected);
    });

    it('removes all selected and hidden list items', () => {
      const state: ReportState = {
        ...initialState,
        selectedListItems: [1, 3, 5],
        hiddenLines: [5],
      };

      const nextState: ReportState = report(state, removeSelectedListItems());

      const expected: ReportState = {...state, selectedListItems: [], hiddenLines: []};
      expect(nextState).toEqual(expected);
    });
  });

});

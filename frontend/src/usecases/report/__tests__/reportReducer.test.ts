import {mockSelectionAction} from '../../../__tests__/testActions';
import {DateRange, Period, TemporalResolution} from '../../../components/dates/dateModels';
import {momentFrom} from '../../../helpers/dateHelpers';
import {Medium, Quantity} from '../../../state/ui/graph/measurement/measurementModels';
import {selectPeriod, setCustomDateRange} from '../../../state/user-selection/userSelectionActions';
import {logoutUser} from '../../auth/authActions';
import {
  hideAllLines,
  removeSelectedListItems,
  selectResolution,
  setReportTimePeriod,
  setSelectedItems,
  toggleLine
} from '../reportActions';
import {ReportState, SelectedReportPayload} from '../reportModels';
import {initialState, report} from '../reportReducer';

describe('reportReducer', () => {
  const items = [{id: 1, label: 'a', medium: Medium.gas}, {id: 2, label: 'b', medium: Medium.water}];

  it('makes sure the legend items is set to payload', () => {
    const payload: SelectedReportPayload = {
      items,
      quantities: [Quantity.energy],
      media: [Medium.districtHeating],
    };

    const nextState: ReportState = report(initialState, setSelectedItems(payload));

    const expected: ReportState = {
      ...initialState,
      savedReports: {meterPage: {id: 'meterPage', meters: items}}
    };
    expect(nextState).toEqual(expected);
  });

  it('same state when the main selection is changed', () => {
    const state: ReportState = {...initialState, savedReports: {meterPage: {id: 'meterPage', meters: items}}};

    const nextState = report(state, mockSelectionAction);

    expect(nextState).toEqual(state);
  });

  describe('logout user', () => {

    it('resets selection', () => {
      const state: ReportState = {...initialState, savedReports: {meterPage: {id: 'meterPage', meters: items}}};

      const nextState: ReportState = report(state, logoutUser(undefined));

      expect(nextState).toEqual(initialState);
    });
  });

  describe('change period', () => {

    it('should not clear selected list items when changing global period', () => {
      const payload: SelectedReportPayload = {
        items,
        quantities: [],
        media: [],
      };
      const state: ReportState = report(initialState, setSelectedItems(payload));

      const expected: ReportState = {
        ...initialState,
        savedReports: {meterPage: {id: 'meterPage', meters: items}},
      };
      expect(state).toEqual(expected);

      const newState: ReportState = report(state, selectPeriod(Period.currentMonth));
      expect(newState).toBe(state);
    });

    it('should have a default time period', () => {
      expect(initialState.timePeriod).toBeTruthy();
    });

    it('can change its time period', () => {
      const afterChange: ReportState = report(initialState, setReportTimePeriod({period: Period.currentMonth}));
      expect(afterChange.timePeriod).not.toEqual(initialState.timePeriod);
      expect(afterChange.timePeriod).toEqual({period: Period.currentMonth});
    });

  });

  describe('change custom date range', () => {

    it('should not clear selected list items', () => {
      const start: Date = momentFrom('2018-12-09').toDate();
      const end: Date = momentFrom('2018-12-24').toDate();
      const dateRange: DateRange = {start, end};

      const items = [{id: 1, label: 'a', medium: Medium.gas}, {id: 2, label: 'b', medium: Medium.water}];
      const payload: SelectedReportPayload = {
        items,
        quantities: [],
        media: [],
      };
      const state: ReportState = report(initialState, setSelectedItems(payload));

      const expected: ReportState = {
        ...initialState,
        savedReports: {meterPage: {id: 'meterPage', meters: items}},
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

      const expected: ReportState = {...initialState, isAllLinesHidden: true};
      expect(state).toEqual(expected);
    });

    it('hides single line', () => {
      const startState: ReportState = {
        ...initialState,
        savedReports: {meterPage: {id: 'meterPage', meters: [items[1]]}}
      };

      const state: ReportState = report(startState, hideAllLines());

      const expected: ReportState = {...startState, hiddenLines: [2], isAllLinesHidden: true};
      expect(state).toEqual(expected);
    });

    it('hides all lines', () => {
      const startState: ReportState = {
        ...initialState,
        savedReports: {meterPage: {id: 'meterPage', meters: items}}
      };

      const state: ReportState = report(startState, hideAllLines());

      const expected: ReportState = {...startState, hiddenLines: [1, 2], isAllLinesHidden: true};
      expect(state).toEqual(expected);
    });

    it('can show all lines again all lines', () => {
      const state: ReportState = {
        ...initialState,
        savedReports: {meterPage: {id: 'meterPage', meters: items}}
      };

      let nextState: ReportState = report(state, hideAllLines());

      let expected: ReportState = {...state, hiddenLines: [1, 2], isAllLinesHidden: true};
      expect(nextState).toEqual(expected);

      nextState = report(nextState, hideAllLines());

      expected = {...state, hiddenLines: [], isAllLinesHidden: false};
      expect(nextState).toEqual(expected);
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
        savedReports: {meterPage: {id: 'meterPage', meters: [items[1]]}}
      };

      const nextState: ReportState = report(state, removeSelectedListItems());

      expect(nextState).toEqual(initialState);
    });

    it('removes all selected list items', () => {
      const state: ReportState = {
        ...initialState,
        savedReports: {meterPage: {id: 'meterPage', meters: items}}
      };

      const nextState: ReportState = report(state, removeSelectedListItems());

      expect(nextState).toEqual(initialState);
    });

    it('removes all selected and hidden list items', () => {
      const state: ReportState = {
        ...initialState,
        hiddenLines: items.map((it) => it.id),
        isAllLinesHidden: true,
        savedReports: {meterPage: {id: 'meterPage', meters: items}},
      };

      const nextState: ReportState = report(state, removeSelectedListItems());

      expect(nextState).toEqual(initialState);
    });
  });

});

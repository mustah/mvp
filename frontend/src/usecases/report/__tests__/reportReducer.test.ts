import {mockSelectionAction} from '../../../__tests__/testActions';
import {savedReportsOf} from '../../../__tests__/testDataFactory';
import {DateRange, Period, TemporalResolution} from '../../../components/dates/dateModels';
import {momentFrom} from '../../../helpers/dateHelpers';
import {ObjectsById} from '../../../state/domain-models/domainModels';
import {Medium, Quantity} from '../../../state/ui/graph/measurement/measurementModels';
import {selectPeriod, setCustomDateRange} from '../../../state/user-selection/userSelectionActions';
import {logoutUser} from '../../auth/authActions';
import {
  hideAllByMedium,
  removeAllByMedium,
  selectResolution,
  setReportTimePeriod,
  setSelectedItems,
  showHideMediumRows,
  toggleLine
} from '../reportActions';

import {LegendItem, Report, ReportState, SelectedReportPayload, ViewOption} from '../reportModels';
import {initialState, report} from '../reportReducer';
import {getMediumViewOptions} from '../reportSelectors';

describe('reportReducer', () => {

  const items = [{id: 1, label: 'a', medium: Medium.gas}, {id: 2, label: 'b', medium: Medium.water}];
  const gasMeter: LegendItem = items[0];
  const waterMeter: LegendItem = items[1];

  const savedReports: ObjectsById<Report> = savedReportsOf(items);

  it('makes sure the legend items is set to payload', () => {
    const payload: SelectedReportPayload = {
      items,
      quantities: [Quantity.energy],
      media: [Medium.districtHeating],
    };

    const nextState: ReportState = report(initialState, setSelectedItems(payload));

    const expected: ReportState = {
      ...initialState,
      savedReports
    };
    expect(nextState).toEqual(expected);
  });

  it('same state when the main selection is changed', () => {
    const state: ReportState = {...initialState, savedReports};

    const nextState = report(state, mockSelectionAction);

    expect(nextState).toEqual(state);
  });

  describe('logout user', () => {

    it('resets selection', () => {
      const state: ReportState = {...initialState, savedReports};

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

      const expected: ReportState = {...initialState, savedReports};
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

      const expected: ReportState = {...initialState, savedReports};

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

  describe('hideAllByMedium', () => {

    it('does nothing with empty saved meter reports', () => {
      const state: ReportState = report(initialState, hideAllByMedium(Medium.electricity));

      expect(state).toEqual(initialState);
    });

    it('hides single item for given medium', () => {
      const state: ReportState = {
        ...initialState,
        savedReports: savedReportsOf([gasMeter])
      };

      const nextState: ReportState = report(state, hideAllByMedium(Medium.gas));

      expect(nextState.hiddenLines).toEqual([gasMeter.id]);
    });

    it('hides all lines for given medium', () => {
      const state: ReportState = {...initialState, savedReports};

      const nextState: ReportState = report(state, hideAllByMedium(Medium.gas));

      expect(nextState.hiddenLines).toEqual([gasMeter.id]);
    });

    it('can show all lines again', () => {
      const state: ReportState = {
        ...initialState,
        savedReports: savedReportsOf([...items, {...gasMeter, id: 5}])
      };

      let nextState: ReportState = report(state, hideAllByMedium(Medium.gas));

      let expected: ViewOption = {isAllLinesHidden: true};
      expect(nextState.hiddenLines).toEqual([1, 5]);
      expect(getMediumViewOptions(nextState)[Medium.gas]).toEqual(expected);

      nextState = report(nextState, hideAllByMedium(Medium.gas));

      expected = {isAllLinesHidden: false};
      expect(nextState.hiddenLines).toEqual([]);
      expect(getMediumViewOptions(nextState)[Medium.gas]).toEqual(expected);
    });
  });

  describe('removeAllByMedium', () => {

    it('can handle empty selected list items', () => {
      const nextState: ReportState = report(initialState, removeAllByMedium(Medium.gas));
      expect(nextState).toEqual(initialState);
    });

    it('removes single selected list item', () => {
      const state: ReportState = {
        ...initialState,
        savedReports: savedReportsOf([items[1]])
      };

      const nextState: ReportState = report(state, removeAllByMedium(Medium.water));

      expect(nextState).toEqual(initialState);
    });

    it('removes all selected list items', () => {
      const state: ReportState = {...initialState, savedReports};

      const nextState: ReportState = report(state, removeAllByMedium(Medium.gas));

      expect(nextState).toEqual({...initialState, savedReports: savedReportsOf([items[1]])});
    });

    it('removes all selected and hidden list items', () => {
      const state: ReportState = {
        ...initialState,
        hiddenLines: items.map((it) => it.id),
        savedReports,
      };

      const nextState: ReportState = report(state, removeAllByMedium(Medium.water));

      expect(nextState).toEqual({...initialState, savedReports: savedReportsOf([items[0]])});
    });

    it('removes all one for each medium', () => {
      const state: ReportState = {
        ...initialState,
        hiddenLines: items.map((it) => it.id),
        savedReports,
      };

      let nextState: ReportState = report(state, removeAllByMedium(Medium.gas));
      nextState = report(nextState, removeAllByMedium(Medium.water));

      expect(nextState).toEqual(initialState);
    });
  });

  describe('showHideMediumRows', () => {

    it('does nothing with items that are not in the report', () => {
      const nextState: ReportState = report(initialState, showHideMediumRows(Medium.gas));
      expect(nextState).toEqual(initialState);
    });

    it('toggles single legend item with same medium', () => {
      const state: ReportState = {
        ...initialState,
        savedReports: savedReportsOf([items[1]])
      };

      const nextState: ReportState = report(state, showHideMediumRows(Medium.water));

      const legendItem: LegendItem = {...waterMeter, isRowExpanded: true};
      expect(nextState).toEqual({...initialState, savedReports: savedReportsOf([legendItem])});
    });

    it('toggles all legend items with same medium', () => {
      const medium = Medium.gas;
      const gasMeter2 = {...items[0], id: 3, medium};
      const state: ReportState = {
        ...initialState,
        savedReports: savedReportsOf([...items, gasMeter2])
      };

      const nextState: ReportState = report(state, showHideMediumRows(medium));

      const item1: LegendItem = {...gasMeter, isRowExpanded: true};
      const item2: LegendItem = {...gasMeter2, isRowExpanded: true};
      expect(nextState).toEqual({...initialState, savedReports: savedReportsOf([item1, waterMeter, item2])});
    });

  });

});

import {mockSelectionAction} from '../../../__tests__/testActions';
import {savedReportsOf} from '../../../__tests__/testDataFactory';
import {DateRange, Period, TemporalResolution} from '../../../components/dates/dateModels';
import {momentFrom} from '../../../helpers/dateHelpers';
import {ObjectsById} from '../../../state/domain-models/domainModels';
import {Medium, Quantity} from '../../../state/ui/graph/measurement/measurementModels';
import {selectPeriod, setCustomDateRange} from '../../../state/user-selection/userSelectionActions';
import {logoutUser} from '../../auth/authActions';
import {
  addLegendItems,
  removeAllByMedium,
  selectResolution,
  setReportTimePeriod,
  showHideAllByMedium,
  showHideMediumRows,
  toggleLine,
  toggleQuantityByMedium
} from '../reportActions';

import {LegendItem, QuantityMedium, Report, ReportState, ViewOptions} from '../reportModels';
import {initialState, report} from '../reportReducer';
import {getHiddenLines, getLegendItems, getViewOptions} from '../reportSelectors';

describe('reportReducer', () => {

  const isHidden = false;
  const quantities: Quantity[] = [];

  const items: LegendItem[] = [
    {id: 1, label: 'a', medium: Medium.gas, isHidden, quantities},
    {id: 2, label: 'b', medium: Medium.water, isHidden, quantities}
  ];
  const gasMeter: LegendItem = items[0];
  const waterMeter: LegendItem = items[1];
  const meter: LegendItem = {id: 4, label: 'dh', medium: Medium.districtHeating, isHidden, quantities};

  const savedReports: ObjectsById<Report> = savedReportsOf(items);

  it('makes sure the legend items is set to payload', () => {
    const nextState: ReportState = report(initialState, addLegendItems(items));

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
      const state: ReportState = report(initialState, addLegendItems(items));

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

      const state: ReportState = report(initialState, addLegendItems(items));

      const expected: ReportState = {...initialState, savedReports};
      expect(state).toEqual(expected);

      const newState: ReportState = report(state, setCustomDateRange(dateRange));
      expect(newState).toBe(state);
    });

  });

  describe('toggleLine', () => {

    it('should add hide legend item id', () => {
      const state: ReportState = {...initialState, savedReports};
      const nextState: ReportState = report(state, toggleLine(gasMeter.id));

      const expected: LegendItem[] = [{...gasMeter, isHidden: true}, waterMeter];
      expect(getLegendItems(nextState)).toEqual(expected);
    });

    it('should remove existing hidden legend item id ', () => {
      const meters: LegendItem[] = [{...waterMeter, isHidden: true}, {...gasMeter, isHidden: true}];
      const state: ReportState = {...initialState, savedReports: savedReportsOf(meters)};
      const nextState: ReportState = report(state, toggleLine(gasMeter.id));

      const expected: LegendItem[] = [{...waterMeter, isHidden: true}, {...gasMeter, isHidden: false}];
      expect(getLegendItems(nextState)).toEqual(expected);
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

  describe('showHideAllByMedium', () => {

    it('does nothing with empty saved meter reports', () => {
      const state: ReportState = report(initialState, showHideAllByMedium(Medium.electricity));

      expect(state).toEqual(initialState);
    });

    it('hides single item for given medium', () => {
      const state: ReportState = {
        ...initialState,
        savedReports: savedReportsOf([gasMeter])
      };

      const nextState: ReportState = report(state, showHideAllByMedium(Medium.gas));

      const expected: LegendItem[] = [{...gasMeter, isHidden: true}];
      expect(getLegendItems(nextState)).toEqual(expected);
    });

    it('hides all lines for given medium', () => {
      const state: ReportState = {
        ...initialState,
        savedReports: savedReportsOf([gasMeter, {...gasMeter, id: 8}])
      };

      const nextState: ReportState = report(state, showHideAllByMedium(Medium.gas));

      const expected: LegendItem[] = [{...gasMeter, isHidden: true}, {...gasMeter, id: 8, isHidden: true}];
      expect(getLegendItems(nextState)).toEqual(expected);
    });

    it('can show all lines again', () => {
      const gasMeter2 = {...gasMeter, id: 5};
      const state: ReportState = {
        ...initialState,
        savedReports: savedReportsOf([...items, gasMeter2])
      };

      const medium = Medium.gas;
      let nextState: ReportState = report(state, showHideAllByMedium(medium));

      let expected: ViewOptions = {isAllLinesHidden: true, quantities};
      const expectedGasItems: LegendItem[] = [{...gasMeter, isHidden: true}, {...gasMeter2, isHidden: true}];
      expect(getLegendItems(nextState)).toEqual(expectedGasItems);
      expect(getHiddenLines(nextState)).toEqual([gasMeter.id, gasMeter2.id]);
      expect(getViewOptions(nextState, medium)).toEqual(expected);

      nextState = report(nextState, showHideAllByMedium(medium));

      expected = {isAllLinesHidden: false, quantities};
      expect(getHiddenLines(nextState)).toEqual([]);
      expect(getViewOptions(nextState, medium)).toEqual(expected);
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
      const state: ReportState = {...initialState, savedReports};

      const nextState: ReportState = report(state, removeAllByMedium(Medium.water));

      expect(nextState).toEqual({...initialState, savedReports: savedReportsOf([items[0]])});
    });

    it('removes all one for each medium', () => {
      const state: ReportState = {...initialState, savedReports};

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

    describe('toggleQuantityByMedium', () => {

      it('selects single quantity for given medium', () => {
        const state: ReportState = {...initialState, savedReports};
        const payload: QuantityMedium = {medium: Medium.gas, quantity: Quantity.volume};

        const nextState: ReportState = report(state, toggleQuantityByMedium(payload));

        expect(getViewOptions(nextState, Medium.gas).quantities).toEqual([Quantity.volume]);

        const expected: LegendItem[] = [{...gasMeter, quantities: [Quantity.volume]}, waterMeter];
        expect(getLegendItems(nextState)).toEqual(expected);
      });

      it('selects more than one quantity for given medium', () => {
        const state: ReportState = {...initialState, savedReports: savedReportsOf([...items, meter])};
        const payload: QuantityMedium = {medium: Medium.districtHeating, quantity: Quantity.power};

        let nextState: ReportState = report(state, toggleQuantityByMedium(payload));
        nextState = report(nextState, toggleQuantityByMedium({...payload, quantity: Quantity.flow}));

        const quantities: Quantity[] = [Quantity.power, Quantity.flow];
        const legendItems: LegendItem[] = [...items, {...meter, quantities}];

        expect(getViewOptions(nextState, Medium.districtHeating).quantities).toEqual(quantities);
        expect(getLegendItems(nextState)).toEqual(legendItems);
      });

      it('de-selects already selected quantity for given medium', () => {
        const state: ReportState = {...initialState, savedReports: savedReportsOf([...items, meter])};
        const payload: QuantityMedium = {medium: Medium.districtHeating, quantity: Quantity.power};

        let nextState: ReportState = report(state, toggleQuantityByMedium(payload));
        nextState = report(nextState, toggleQuantityByMedium({...payload, quantity: Quantity.flow}));
        nextState = report(nextState, toggleQuantityByMedium({...payload, quantity: Quantity.flow}));

        const quantities: Quantity[] = [Quantity.power];
        const legendItems: LegendItem[] = [...items, {...meter, quantities}];

        expect(getViewOptions(nextState, Medium.districtHeating).quantities).toEqual(quantities);
        expect(getLegendItems(nextState)).toEqual(legendItems);
      });

    });

  });

});

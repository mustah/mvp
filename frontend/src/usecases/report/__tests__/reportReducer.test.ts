import {mockSelectionAction} from '../../../__tests__/testActions';
import {savedReportsWith} from '../../../__tests__/testDataFactory';
import {DateRange, Period, TemporalResolution} from '../../../components/dates/dateModels';
import {momentAtUtcPlusOneFrom} from '../../../helpers/dateHelpers';
import {Medium, Quantity} from '../../../state/ui/graph/measurement/measurementModels';
import {selectPeriod, setCustomDateRange} from '../../../state/user-selection/userSelectionActions';
import {SelectionInterval} from '../../../state/user-selection/userSelectionModels';
import {logoutUser} from '../../auth/authActions';
import {
  addLegendItems,
  removeAllByMedium,
  selectResolution,
  setReportTimePeriod,
  showHideAllByMedium,
  showHideMediumRows,
  toggleLine,
  toggleQuantityById,
  toggleQuantityByMedium
} from '../reportActions';

import {
  LegendItem,
  QuantityId,
  QuantityMedium,
  ReportState,
  SavedReportsState,
  TemporalReportState,
  ViewOptions
} from '../reportModels';
import {
  initialSavedReportState,
  initialState,
  initialTemporalState,
  report,
  savedReports,
  temporal
} from '../reportReducer';
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

  const savedReportsState: SavedReportsState = savedReportsWith(items);

  it('makes sure the legend items is set to payload', () => {
    const nextState: ReportState = report(initialState, addLegendItems(items));

    const expected: ReportState = {
      ...initialState,
      savedReports: savedReportsState
    };
    expect(nextState).toEqual(expected);
  });

  it('same state when the main selection is changed', () => {
    const state: ReportState = {...initialState, savedReports: savedReportsState};

    const nextState = report(state, mockSelectionAction);

    expect(nextState).toEqual(state);
  });

  describe('logout user', () => {

    it('resets selection', () => {
      const state: ReportState = {...initialState, savedReports: savedReportsState};

      const nextState: ReportState = report(state, logoutUser(undefined));

      expect(nextState).toEqual(initialState);
    });
  });

  describe('change period', () => {

    it('should not clear selected list items when changing global period', () => {
      const state: TemporalReportState = temporal(initialTemporalState, addLegendItems(items));

      expect(state).toEqual(initialTemporalState);

      const newState: TemporalReportState = temporal(state, selectPeriod(Period.currentMonth));
      expect(newState).toBe(state);
    });

    it('can change its time period', () => {
      const action = setReportTimePeriod({period: Period.currentMonth});

      const afterChange: TemporalReportState = temporal(initialTemporalState, action);

      const expected: SelectionInterval = {period: Period.currentMonth};
      expect(afterChange.timePeriod).toEqual(expected);
    });

  });

  describe('change custom date range', () => {

    it('should not clear selected list items', () => {
      const start: Date = momentAtUtcPlusOneFrom('2018-12-09').toDate();
      const end: Date = momentAtUtcPlusOneFrom('2018-12-24').toDate();
      const dateRange: DateRange = {start, end};

      const state: ReportState = report(initialState, addLegendItems(items));

      const expected: ReportState = {...initialState, savedReports: savedReportsState};
      expect(state).toEqual(expected);

      const newState: ReportState = report(state, setCustomDateRange(dateRange));
      expect(newState).toBe(state);
    });

  });

  describe('toggleLine', () => {

    it('should add hide legend item id', () => {
      const nextState: SavedReportsState = savedReports(savedReportsState, toggleLine(gasMeter.id));

      const expected: LegendItem[] = [{...gasMeter, isHidden: true}, waterMeter];
      expect(getLegendItems(nextState)).toEqual(expected);
    });

    it('should remove existing hidden legend item id ', () => {
      const meters: LegendItem[] = [{...waterMeter, isHidden: true}, {...gasMeter, isHidden: true}];

      const nextState: SavedReportsState = savedReports(savedReportsWith(meters), toggleLine(gasMeter.id));

      const expected: LegendItem[] = [{...waterMeter, isHidden: true}, {...gasMeter, isHidden: false}];
      expect(getLegendItems(nextState)).toEqual(expected);
    });
  });

  describe('selectResolution', () => {

    it('can select hourly resolution', () => {
      const payload = TemporalResolution.hour;

      const state: TemporalReportState = temporal(initialTemporalState, selectResolution(payload));

      const expected: TemporalReportState = {...initialTemporalState, resolution: payload};
      expect(state).toEqual(expected);
    });

    it('changes resolution', () => {
      const payload = TemporalResolution.hour;

      let state: TemporalReportState = temporal(initialTemporalState, selectResolution(payload));

      let expected: TemporalReportState = {...initialTemporalState, resolution: payload};
      expect(state).toEqual(expected);

      state = temporal(initialTemporalState, selectResolution(TemporalResolution.month));

      expected = {...initialTemporalState, resolution: TemporalResolution.month};
      expect(state).toEqual(expected);
    });
  });

  describe('showHideAllByMedium', () => {

    it('does nothing with empty saved meter reports', () => {
      const state: SavedReportsState = savedReports(savedReportsState, showHideAllByMedium(Medium.electricity));

      expect(state).toEqual(initialSavedReportState);
    });

    it('hides single item for given medium', () => {
      const nextState: SavedReportsState = savedReports(savedReportsWith([gasMeter]), showHideAllByMedium(Medium.gas));

      const expected: LegendItem[] = [{...gasMeter, isHidden: true}];
      expect(getLegendItems(nextState)).toEqual(expected);
    });

    it('hides all lines for given medium', () => {
      const state: SavedReportsState = savedReportsWith([gasMeter, {...gasMeter, id: 8}]);

      const nextState: SavedReportsState = savedReports(state, showHideAllByMedium(Medium.gas));

      const expected: LegendItem[] = [{...gasMeter, isHidden: true}, {...gasMeter, id: 8, isHidden: true}];
      expect(getLegendItems(nextState)).toEqual(expected);
    });

    it('can show all lines again', () => {
      const gasMeter2 = {...gasMeter, id: 5};
      const state: SavedReportsState = savedReportsWith([...items, gasMeter2]);

      const medium = Medium.gas;
      let nextState: SavedReportsState = savedReports(state, showHideAllByMedium(medium));

      let expected: ViewOptions = {isAllLinesHidden: true, quantities};
      const expectedGasItems: LegendItem[] = [{...gasMeter, isHidden: true}, {...gasMeter2, isHidden: true}];
      expect(getLegendItems(nextState)).toEqual(expectedGasItems);
      expect(getHiddenLines(nextState)).toEqual([gasMeter.id, gasMeter2.id]);
      expect(getViewOptions(nextState, medium)).toEqual(expected);

      nextState = savedReports(nextState, showHideAllByMedium(medium));

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
        savedReports: savedReportsWith([items[1]])
      };

      const nextState: ReportState = report(state, removeAllByMedium(Medium.water));

      expect(nextState).toEqual(initialState);
    });

    it('removes all selected list items', () => {
      const state: ReportState = {...initialState, savedReports: savedReportsState};

      const nextState: ReportState = report(state, removeAllByMedium(Medium.gas));

      expect(nextState).toEqual({...initialState, savedReports: savedReportsWith([items[1]])});
    });

    it('removes all selected and hidden list items', () => {
      const state: ReportState = {...initialState, savedReports: savedReportsState};

      const nextState: ReportState = report(state, removeAllByMedium(Medium.water));

      expect(nextState).toEqual({...initialState, savedReports: savedReportsWith([items[0]])});
    });

    it('removes all one for each medium', () => {
      const state: ReportState = {...initialState, savedReports: savedReportsState};

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
        savedReports: savedReportsWith([items[1]])
      };

      const nextState: ReportState = report(state, showHideMediumRows(Medium.water));

      const legendItem: LegendItem = {...waterMeter, isRowExpanded: true};
      expect(nextState).toEqual({...initialState, savedReports: savedReportsWith([legendItem])});
    });

    it('toggles all legend items with same medium', () => {
      const medium = Medium.gas;
      const gasMeter2 = {...items[0], id: 3, medium};
      const state: ReportState = {
        ...initialState,
        savedReports: savedReportsWith([...items, gasMeter2])
      };

      const nextState: ReportState = report(state, showHideMediumRows(medium));

      const item1: LegendItem = {...gasMeter, isRowExpanded: true};
      const item2: LegendItem = {...gasMeter2, isRowExpanded: true};
      expect(nextState).toEqual({...initialState, savedReports: savedReportsWith([item1, waterMeter, item2])});
    });

    describe('toggleQuantityByMedium', () => {

      it('selects single quantity for given medium', () => {
        const payload: QuantityMedium = {medium: Medium.gas, quantity: Quantity.volume};

        const nextState: SavedReportsState = savedReports(savedReportsState, toggleQuantityByMedium(payload));

        expect(getViewOptions(nextState, Medium.gas).quantities).toEqual([Quantity.volume]);

        const expected: LegendItem[] = [{...gasMeter, quantities: [Quantity.volume]}, waterMeter];
        expect(getLegendItems(nextState)).toEqual(expected);
      });

      it('selects more than one quantity for given medium', () => {
        const state: SavedReportsState = savedReportsWith([...items, meter]);
        const payload: QuantityMedium = {medium: Medium.districtHeating, quantity: Quantity.power};

        let nextState: SavedReportsState = savedReports(state, toggleQuantityByMedium(payload));
        nextState = savedReports(nextState, toggleQuantityByMedium({...payload, quantity: Quantity.flow}));

        const quantities: Quantity[] = [Quantity.power, Quantity.flow];
        const legendItems: LegendItem[] = [...items, {...meter, quantities}];

        expect(getViewOptions(nextState, Medium.districtHeating).quantities).toEqual(quantities);
        expect(getLegendItems(nextState)).toEqual(legendItems);
      });

      it('de-selects already selected quantity for given medium', () => {
        const state: SavedReportsState = savedReportsWith([...items, meter]);
        const payload: QuantityMedium = {medium: Medium.districtHeating, quantity: Quantity.power};

        let nextState: SavedReportsState = savedReports(state, toggleQuantityByMedium(payload));
        nextState = savedReports(nextState, toggleQuantityByMedium({...payload, quantity: Quantity.flow}));
        nextState = savedReports(nextState, toggleQuantityByMedium({...payload, quantity: Quantity.flow}));

        const quantities: Quantity[] = [Quantity.power];
        const legendItems: LegendItem[] = [...items, {...meter, quantities}];

        expect(getViewOptions(nextState, Medium.districtHeating).quantities).toEqual(quantities);
        expect(getLegendItems(nextState)).toEqual(legendItems);
      });

    });

    describe('toggleQuantityById', () => {

      it('selects single quantity for given legend item', () => {
        const payload: QuantityId = {id: gasMeter.id, quantity: Quantity.volume};

        const nextState: SavedReportsState = savedReports(savedReportsState, toggleQuantityById(payload));

        const expected: LegendItem[] = [{...gasMeter, quantities: [Quantity.volume]}, waterMeter];
        expect(getLegendItems(nextState)).toEqual(expected);
      });

      it('de-selects selected quantity for given legend item', () => {
        const meters = [{...gasMeter, quantities: [Quantity.volume]}, waterMeter];
        const state: SavedReportsState = savedReportsWith(meters);
        const payload: QuantityId = {id: gasMeter.id, quantity: Quantity.volume};

        const nextState: SavedReportsState = savedReports(state, toggleQuantityById(payload));

        expect(getLegendItems(nextState)).toEqual(items);
      });

      it('does nothing when id does not exist', () => {
        const payload: QuantityId = {id: -999, quantity: Quantity.volume};

        const nextState: SavedReportsState = savedReports(savedReportsState, toggleQuantityById(payload));

        expect(getLegendItems(nextState)).toEqual(items);
      });

    });

  });

});

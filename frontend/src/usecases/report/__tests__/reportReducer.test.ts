import {mockSelectionAction} from '../../../__tests__/testActions';
import {savedReportsWith} from '../../../__tests__/testDataFactory';
import {DateRange} from '../../../components/dates/dateModels';
import {momentAtUtcPlusOneFrom} from '../../../helpers/dateHelpers';
import {Medium, Quantity} from '../../../state/ui/graph/measurement/measurementModels';
import {setCustomDateRange} from '../../../state/user-selection/userSelectionActions';
import {logoutUser} from '../../auth/authActions';
import {
  addLegendItems,
  removeAllByType,
  showHideAllByType,
  showHideLegendRows,
  toggleLine,
  toggleQuantityById,
  toggleQuantityByType
} from '../reportActions';

import {LegendItem, QuantityId, QuantityLegendType, ReportState, SavedReportsState, ViewOptions} from '../reportModels';
import {initialSavedReportState, initialState, report, savedReports} from '../reportReducer';
import {getHiddenLines, getLegendItems, getViewOptions} from '../reportSelectors';

describe('reportReducer', () => {

  const isHidden = false;
  const quantities: Quantity[] = [];

  const items: LegendItem[] = [
    {id: 1, label: 'a', type: Medium.gas, isHidden, quantities},
    {id: 2, label: 'b', type: Medium.water, isHidden, quantities}
  ];
  const gasMeter: LegendItem = items[0];
  const waterMeter: LegendItem = items[1];
  const meter: LegendItem = {id: 4, label: 'dh', type: Medium.districtHeating, isHidden, quantities};

  const savedReportsState: SavedReportsState = savedReportsWith(items);
  const emptySavedReportsState: SavedReportsState = savedReportsWith([]);
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
      const legendItems: LegendItem[] = [{...waterMeter, isHidden: true}, {...gasMeter, isHidden: true}];

      const nextState: SavedReportsState = savedReports(savedReportsWith(legendItems), toggleLine(gasMeter.id));

      const expected: LegendItem[] = [{...waterMeter, isHidden: true}, {...gasMeter, isHidden: false}];
      expect(getLegendItems(nextState)).toEqual(expected);
    });
  });

  describe('showHideAllByType', () => {

    it('does nothing with empty saved meter reports', () => {
      const state: SavedReportsState = savedReports(emptySavedReportsState, showHideAllByType(Medium.electricity));

      expect(state).toEqual(initialSavedReportState);
    });

    it('hides single item for given type', () => {
      const nextState: SavedReportsState = savedReports(savedReportsWith([gasMeter]), showHideAllByType(Medium.gas));

      const expected: LegendItem[] = [{...gasMeter, isHidden: true}];
      expect(getLegendItems(nextState)).toEqual(expected);
    });

    it('hides all lines for given type', () => {
      const state: SavedReportsState = savedReportsWith([gasMeter, {...gasMeter, id: 8}]);

      const nextState: SavedReportsState = savedReports(state, showHideAllByType(Medium.gas));

      const expected: LegendItem[] = [{...gasMeter, isHidden: true}, {...gasMeter, id: 8, isHidden: true}];
      expect(getLegendItems(nextState)).toEqual(expected);
    });

    it('does not affect lines for other type', () => {
      const state: SavedReportsState = savedReportsWith([gasMeter]);

      const nextState: SavedReportsState = savedReports(state, showHideAllByType(Medium.districtHeating));

      const expected: LegendItem[] = [gasMeter];
      expect(getLegendItems(nextState)).toEqual(expected);
    });

    it('only hides lines for given type', () => {
      const state: SavedReportsState = savedReportsWith([gasMeter, waterMeter]);

      const nextState: SavedReportsState = savedReports(state, showHideAllByType(Medium.water));

      const expected: LegendItem[] = [gasMeter, {...waterMeter, isHidden: true}];
      expect(getLegendItems(nextState)).toEqual(expected);
    });

    it('can show all lines again', () => {
      const gasMeter2 = {...gasMeter, id: 5};
      const state: SavedReportsState = savedReportsWith([gasMeter, gasMeter2]);

      const medium = Medium.gas;
      let nextState: SavedReportsState = savedReports(state, showHideAllByType(medium));

      let expected: ViewOptions = {isAllLinesHidden: true, quantities};
      const expectedGasItems: LegendItem[] = [{...gasMeter, isHidden: true}, {...gasMeter2, isHidden: true}];
      expect(getLegendItems(nextState)).toEqual(expectedGasItems);
      expect(getHiddenLines(nextState)).toEqual([gasMeter.id, gasMeter2.id]);
      expect(getViewOptions(nextState, medium)).toEqual(expected);

      nextState = savedReports(nextState, showHideAllByType(medium));

      expected = {isAllLinesHidden: false, quantities};
      expect(getHiddenLines(nextState)).toEqual([]);
      expect(getViewOptions(nextState, medium)).toEqual(expected);
    });
  });

  describe('removeAllByType', () => {

    it('can handle empty selected list items', () => {
      const nextState: ReportState = report(initialState, removeAllByType(Medium.gas));
      expect(nextState).toEqual(initialState);
    });

    it('removes single selected list item', () => {
      const state: ReportState = {
        ...initialState,
        savedReports: savedReportsWith([items[1]])
      };

      const nextState: ReportState = report(state, removeAllByType(Medium.water));

      expect(nextState).toEqual(initialState);
    });

    it('removes all selected list items', () => {
      const state: ReportState = {...initialState, savedReports: savedReportsState};

      const nextState: ReportState = report(state, removeAllByType(Medium.gas));

      expect(nextState).toEqual({...initialState, savedReports: savedReportsWith([items[1]])});
    });

    it('removes all selected and hidden list items', () => {
      const state: ReportState = {...initialState, savedReports: savedReportsState};

      const nextState: ReportState = report(state, removeAllByType(Medium.water));

      expect(nextState).toEqual({...initialState, savedReports: savedReportsWith([items[0]])});
    });

    it('removes all one for each type', () => {
      const state: ReportState = {...initialState, savedReports: savedReportsState};

      let nextState: ReportState = report(state, removeAllByType(Medium.gas));
      nextState = report(nextState, removeAllByType(Medium.water));

      expect(nextState).toEqual(initialState);
    });

    it('resets view options for given legend type', () => {
      const payload: QuantityLegendType = {type: Medium.gas, quantity: Quantity.volume};

      let nextState: SavedReportsState = savedReports(savedReportsWith([gasMeter]), toggleQuantityByType(payload));

      expect(getViewOptions(nextState, Medium.gas).quantities).toEqual([Quantity.volume]);

      nextState = savedReports(nextState, removeAllByType(Medium.gas));

      expect(nextState).toEqual(initialSavedReportState);
    });

    it('resets view options for given legend type but keeps view options for other type', () => {
      const payload1: QuantityLegendType = {type: Medium.gas, quantity: Quantity.volume};
      const payload2: QuantityLegendType = {type: Medium.water, quantity: Quantity.volume};

      const state = savedReportsWith(items);

      let nextState: SavedReportsState = savedReports(state, toggleQuantityByType(payload1));
      nextState = savedReports(nextState, toggleQuantityByType(payload2));

      expect(getViewOptions(nextState, Medium.gas).quantities).toEqual([Quantity.volume]);
      expect(getViewOptions(nextState, Medium.water).quantities).toEqual([Quantity.volume]);

      nextState = savedReports(nextState, removeAllByType(Medium.gas));

      const expected: SavedReportsState = {
        ...initialSavedReportState,
        meterPage: {
          ...initialSavedReportState.meterPage,
          legendItems: [{...waterMeter, quantities: [Quantity.volume]}],
          legendViewOptions: {
            ...initialSavedReportState.meterPage.legendViewOptions,
            [Medium.water]: {quantities: [Quantity.volume]},
            [Medium.gas]: {quantities: []},
          }
        }
      };
      expect(nextState).toEqual(expected);
    });

  });

  describe('showHideLegendRows', () => {

    it('does nothing with items that are not in the report', () => {
      const nextState: ReportState = report(initialState, showHideLegendRows(Medium.gas));
      expect(nextState).toEqual(initialState);
    });

    it('toggles single legend item with same type', () => {
      const state: ReportState = {
        ...initialState,
        savedReports: savedReportsWith([items[1]])
      };

      const nextState: ReportState = report(state, showHideLegendRows(Medium.water));

      const legendItem: LegendItem = {...waterMeter, isRowExpanded: true};
      expect(nextState).toEqual({...initialState, savedReports: savedReportsWith([legendItem])});
    });

    it('toggles all legend items with same type', () => {
      const medium = Medium.gas;
      const gasMeter2: LegendItem = {...items[0], id: 3, type: medium};
      const state: ReportState = {
        ...initialState,
        savedReports: savedReportsWith([...items, gasMeter2])
      };

      const nextState: ReportState = report(state, showHideLegendRows(medium));

      const item1: LegendItem = {...gasMeter, isRowExpanded: true};
      const item2: LegendItem = {...gasMeter2, isRowExpanded: true};
      expect(nextState).toEqual({...initialState, savedReports: savedReportsWith([item1, waterMeter, item2])});
    });

    describe('toggleQuantityByType', () => {

      it('selects single quantity for given type', () => {
        const payload: QuantityLegendType = {type: Medium.gas, quantity: Quantity.volume};

        const nextState: SavedReportsState = savedReports(savedReportsState, toggleQuantityByType(payload));

        expect(getViewOptions(nextState, Medium.gas).quantities).toEqual([Quantity.volume]);

        const expected: LegendItem[] = [{...gasMeter, quantities: [Quantity.volume]}, waterMeter];
        expect(getLegendItems(nextState)).toEqual(expected);
      });

      it('selects more than one quantity for given type', () => {
        const state: SavedReportsState = savedReportsWith([...items, meter]);
        const payload: QuantityLegendType = {type: Medium.districtHeating, quantity: Quantity.power};

        let nextState: SavedReportsState = savedReports(state, toggleQuantityByType(payload));
        nextState = savedReports(nextState, toggleQuantityByType({...payload, quantity: Quantity.flow}));

        const quantities: Quantity[] = [Quantity.power, Quantity.flow];
        const legendItems: LegendItem[] = [...items, {...meter, quantities}];

        expect(getViewOptions(nextState, Medium.districtHeating).quantities).toEqual(quantities);
        expect(getLegendItems(nextState)).toEqual(legendItems);
      });

      it('de-selects already selected quantity for given type', () => {
        const state: SavedReportsState = savedReportsWith([...items, meter]);
        const payload: QuantityLegendType = {type: Medium.districtHeating, quantity: Quantity.power};

        let nextState: SavedReportsState = savedReports(state, toggleQuantityByType(payload));
        nextState = savedReports(nextState, toggleQuantityByType({...payload, quantity: Quantity.flow}));
        nextState = savedReports(nextState, toggleQuantityByType({...payload, quantity: Quantity.flow}));

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

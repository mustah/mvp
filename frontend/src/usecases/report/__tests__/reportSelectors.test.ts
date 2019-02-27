import {savedReportsOf} from '../../../__tests__/testDataFactory';
import {
  allQuantities,
  Medium,
  Quantity,
  quantityAttributes
} from '../../../state/ui/graph/measurement/measurementModels';
import {RelationalOperator, ThresholdQuery} from '../../../state/user-selection/userSelectionModels';
import {LegendItem, ReportState, SelectedQuantityColumns} from '../reportModels';
import {initialState} from '../reportReducer';
import {
  getLegendItems,
  getSelectedQuantityColumns,
  getThresholdMedia,
  makeMediumQuantitiesMap
} from '../reportSelectors';

describe('reportSelectors', () => {

  const meter: LegendItem = {
    id: 1,
    label: 'extId1',
    medium: Medium.water,
    isHidden: false,
    quantities: [allQuantities[Medium.water][0]]
  };

  const meter2: LegendItem = {
    id: 2,
    label: 'extId2',
    medium: Medium.gas,
    isHidden: false,
    quantities: [allQuantities[Medium.gas][0]]
  };

  describe('getLegendItems', () => {

    it('has no selected items', () => {
      const expected: LegendItem[] = [];
      expect(getLegendItems(initialState)).toEqual(expected);
    });

    it('has one saved meter', () => {
      const state: ReportState = {
        ...initialState,
        savedReports: savedReportsOf([meter]),
      };

      const expected: LegendItem[] = [meter];
      expect(getLegendItems(state)).toEqual(expected);
    });

    it('has two saved meters', () => {
      const state: ReportState = {
        ...initialState,
        savedReports: savedReportsOf([meter, meter2]),
      };

      const expected: LegendItem[] = [meter, meter2];
      expect(getLegendItems(state)).toEqual(expected);
    });
  });

  describe('getSelectedQuantityColumns', () => {

    it('collects all selected quantities for each medium', () => {
      const districtHeating: LegendItem = {...meter, medium: Medium.districtHeating};
      const meter1: LegendItem = {...districtHeating, id: 1, quantities: [Quantity.flow]};
      const meter2: LegendItem = {...districtHeating, id: 2, quantities: [Quantity.power]};
      const meter3: LegendItem = {...districtHeating, id: 3, quantities: [Quantity.flow]};
      const state: ReportState = {
        ...initialState,
        savedReports: savedReportsOf([meter1, meter2, meter3]),
      };

      const expected: SelectedQuantityColumns = {
        ...makeMediumQuantitiesMap(),
        [Medium.districtHeating]: [Quantity.flow, Quantity.power],
      };
      expect(getSelectedQuantityColumns(state)).toEqual(expected);
    });

  });

  describe('getThresholdMedia', () => {

    it('get media from selected threshold', () => {
      const state: ThresholdQuery = {
        quantity: Quantity.returnTemperature,
        relationalOperator: RelationalOperator.lt,
        unit: quantityAttributes[Quantity.returnTemperature].unit,
        value: '0',
      };

      const expected: Medium[] = [Medium.districtHeating];

      expect(getThresholdMedia(state)).toEqual(expected);
    });

  });

});

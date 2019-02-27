import {savedReportsOf} from '../../../__tests__/testDataFactory';
import {
  allQuantities,
  Medium,
  Quantity,
  quantityAttributes
} from '../../../state/ui/graph/measurement/measurementModels';
import {RelationalOperator, ThresholdQuery} from '../../../state/user-selection/userSelectionModels';
import {LegendItem, ReportState} from '../reportModels';
import {initialState} from '../reportReducer';
import {getLegendItems, getThresholdMedia} from '../reportSelectors';

describe('reportSelectors', () => {

  describe('getLegendItems', () => {

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

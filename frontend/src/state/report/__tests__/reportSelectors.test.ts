import {savedReportsWith} from '../../../__tests__/testDataFactory';
import {idGenerator} from '../../../helpers/idGenerator';
import {makeColumnQuantities} from '../../../usecases/report/helpers/legendHelper';
import {allQuantitiesMap, Medium, Quantity} from '../../ui/graph/measurement/measurementModels';
import {LegendItem, SavedReportsState, SelectedQuantities} from '../reportModels';
import {initialSavedReportState} from '../reportReducer';
import {
  getLegendItems,
  getSelectedQuantitiesMap,
  getVisibilitySummary,
  makeLegendTypeQuantitiesMap
} from '../reportSelectors';

describe('reportSelectors', () => {

  const meter: LegendItem = {
    id: 1,
    label: 'extId1',
    type: Medium.water,
    isHidden: false,
    quantities: [allQuantitiesMap[Medium.water][0]]
  };

  const meter2: LegendItem = {
    id: 2,
    label: 'extId2',
    type: Medium.gas,
    isHidden: false,
    quantities: [allQuantitiesMap[Medium.gas][0]]
  };

  describe('getLegendItems', () => {

    it('has no selected items', () => {
      const expected: LegendItem[] = [];
      expect(getLegendItems(initialSavedReportState)).toEqual(expected);
    });

    it('has one saved meter', () => {
      const state: SavedReportsState = {
        ...initialSavedReportState,
        ...savedReportsWith([meter]),
      };

      const expected: LegendItem[] = [meter];
      expect(getLegendItems(state)).toEqual(expected);
    });

    it('has two saved meters', () => {
      const state: SavedReportsState = {
        ...initialSavedReportState,
        ...savedReportsWith([meter, meter2]),
      };

      const expected: LegendItem[] = [meter, meter2];
      expect(getLegendItems(state)).toEqual(expected);
    });
  });

  describe('getSelectedQuantitiesMap', () => {

    it('collects all selected quantities for each type', () => {
      const districtHeating: LegendItem = {...meter, type: Medium.districtHeating};
      const meter1: LegendItem = {...districtHeating, id: 1, quantities: [Quantity.flow]};
      const meter2: LegendItem = {...districtHeating, id: 2, quantities: [Quantity.power]};
      const meter3: LegendItem = {...districtHeating, id: 3, quantities: [Quantity.flow]};
      const state: SavedReportsState = {
        ...initialSavedReportState,
        ...savedReportsWith([meter1, meter2, meter3]),
      };

      const expected: SelectedQuantities = {
        ...makeLegendTypeQuantitiesMap(),
        [Medium.districtHeating]: [Quantity.flow, Quantity.power],
      };
      expect(getSelectedQuantitiesMap(state)).toEqual(expected);
    });

  });

  describe('makeColumnQuantities', () => {

    it('has no quantities selected when no', () => {
      expect(makeColumnQuantities(initialSavedReportState)).toEqual([]);
    });

    it('collects all selected quantities for each type', () => {
      const meter1: LegendItem = {...meter, id: 1, type: Medium.districtHeating};
      const meter2: LegendItem = {...meter, id: 3, type: Medium.roomSensor};

      const expected: Quantity[] = [...allQuantitiesMap.districtHeating, ...allQuantitiesMap.roomSensor];

      expect(makeColumnQuantities(savedReportsWith([meter1, meter2, meter2]))).toEqual(expected);
    });

  });

  describe('getVisibilitySummary', () => {

    it('counts distinct legend items', () => {
      const firstId = idGenerator.uuid();

      const state: SavedReportsState = {
        meterPage: {
          ...initialSavedReportState.meterPage,
          legendItems: [
            {
              id: firstId,
              label: '_',
              type: Medium.districtHeating,
              quantities: [
                Quantity.differenceTemperature,
                Quantity.energy,
              ],
            },
          ],
        }
      };

      expect(getVisibilitySummary(state)).toEqual({
        allMeters: [firstId],
        checkedMeters: [firstId],
      });
    });

    it('disregards deselected legend items', () => {
      const firstId = idGenerator.uuid();

      const state: SavedReportsState = {
        meterPage: {
          ...initialSavedReportState.meterPage,
          legendItems: [
            {
              id: firstId,
              label: '_',
              type: Medium.districtHeating,
              quantities: [],
            },
          ],
        }
      };

      expect(getVisibilitySummary(state)).toEqual({
        allMeters: [firstId],
        checkedMeters: [],
      });
    });

  });

});

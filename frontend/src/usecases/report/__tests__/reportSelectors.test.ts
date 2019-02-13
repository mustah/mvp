import {Normalized} from '../../../state/domain-models/domainModels';
import {
  SelectedTreeEntities,
  SelectionTreeCity,
  SelectionTreeMeter,
} from '../../../state/selection-tree/selectionTreeModels';
import {getThresholdMedia} from '../../../state/selection-tree/selectionTreeSelectors';
import {Medium, Quantity, quantityAttributes} from '../../../state/ui/graph/measurement/measurementModels';
import {RelationalOperator, ThresholdQuery} from '../../../state/user-selection/userSelectionModels';
import {LegendItem} from '../reportModels';
import {getLegendItems} from '../reportSelectors';

describe('reportSelectors', () => {

  describe('getLegendItems', () => {

    const selectedTreeState: SelectedTreeEntities = {
      selectedListItems: [],
      entities: {cities: {}, meters: {}, addresses: {}},
    };

    const meter: SelectionTreeMeter = {
      id: 1,
      address: 'kabelgatan 2',
      city: 'sweden,kungsbacka',
      name: 'extId1',
      medium: Medium.water,
    };

    const city: SelectionTreeCity = {
      id: 'sweden,kungsbacka',
      city: 'sweden,kungsbacka',
      medium: [Medium.water],
      name: 'kungsbacka',
      addresses: ['sweden,kungsbacka,kabelgatan 2', 'sweden,kungsbacka,kabelgatan 3'],
    };

    describe('selected meters', () => {

      it('has no selected items', () => {
        const expected: Normalized<LegendItem> = {entities: {}, result: []};

        expect(getLegendItems(selectedTreeState)).toEqual(expected);
      });

      it('has selected items but no entities', () => {
        const state: SelectedTreeEntities = {
          ...selectedTreeState,
          selectedListItems: [1, 2, 3],
        };

        const expected: Normalized<LegendItem> = {entities: {}, result: []};

        expect(getLegendItems(state)).toEqual(expected);
      });

      it('selected legend for meter', () => {
        const state: SelectedTreeEntities = {
          ...selectedTreeState,
          entities: {
            ...selectedTreeState.entities,
            meters: {1: meter},
          },
          selectedListItems: [1],
        };

        const expected: Normalized<LegendItem> = {
          entities: {
            lines: {
              1: {
                id: 1,
                facility: 'extId1',
                address: 'kabelgatan 2',
                city: 'Kungsbacka',
                medium: Medium.water,
              },
            },
          },
          result: [1],
        };

        expect(getLegendItems(state)).toEqual(expected);
      });
    });

    describe('selected cities', () => {

      it('has no cities entities then the result is empty', () => {
        const state: SelectedTreeEntities = {
          ...selectedTreeState,
          entities: {
            ...selectedTreeState.entities,
            meters: {1: meter},
          },
          selectedListItems: [city.id],
        };

        const expected: Normalized<LegendItem> = {entities: {}, result: []};

        expect(getLegendItems(state)).toEqual(expected);
      });

      it('selected legend for city', () => {
        const state: SelectedTreeEntities = {
          ...selectedTreeState,
          entities: {
            ...selectedTreeState.entities,
            cities: {[city.id]: city},
          },
          selectedListItems: [city.id],
        };

        const expected: Normalized<LegendItem> = {
          entities: {
            lines: {
              [city.id]: {
                id: city.id,
                city: city.name,
                medium: [Medium.water],
              },
            },
          },
          result: [city.id],
        };

        expect(getLegendItems(state)).toEqual(expected);
      });
    });

    describe('selected cities and meters', () => {

      it('display legend for selected city and meter', () => {
        const state: SelectedTreeEntities = {
          ...selectedTreeState,
          entities: {
            ...selectedTreeState.entities,
            meters: {[meter.id]: meter},
            cities: {[city.id]: city},
          },
          selectedListItems: [meter.id, city.id],
        };

        const expected: Normalized<LegendItem> = {
          entities: {
            lines: {
              [meter.id]: {
                id: meter.id,
                facility: meter.name,
                address: meter.address,
                city: 'Kungsbacka',
                medium: Medium.water,
              },
              [city.id]: {
                id: city.id,
                city: city.name,
                medium: [Medium.water],
              },
            },
          },
          result: [city.id, meter.id],
        };

        expect(getLegendItems(state)).toEqual(expected);
      });
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

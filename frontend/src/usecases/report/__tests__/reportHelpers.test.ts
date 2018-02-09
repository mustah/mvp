import {ObjectsById} from '../../../state/domain-models/domainModels';
import {Measurement} from '../../../state/domain-models/measurement/measurementModels';
import {mapNormalizedPaginatedResultToGraphData} from '../reportHelpers';
import {GraphContents} from '../reportModels';

describe('reportHelpers', () => {
  describe('mapNormalizedPaginatedResultToGraphData', () => {
    describe('formats data for Rechart\'s LineGraph', () => {
      const emptyGraphContents = (): GraphContents => ({
        axes: {
          left: undefined,
          right: undefined,
        },
        lines: [],
        data: [],
      });

      it('handles 0 entities gracefully', () => {
        const graphDataFromZeroEntities = mapNormalizedPaginatedResultToGraphData({});
        expect(graphDataFromZeroEntities).toEqual(emptyGraphContents());
      });
    });

    describe('axes', () => {
      it('extracts a single axis if all measurements are of the same unit', () => {
        const sameUnit: ObjectsById<Measurement> = {
          1: {
            id: 1,
            quantity: 'Power',
            value: 0.4353763591158477,
            unit: 'mW',
            created: 1516521585107,
            physicalMeter: {
              id: 1,
            },
          },
          10: {
            id: 10,
            quantity: 'Power',
            value: 0.22359517968389853,
            unit: 'mW',
            created: 1516529685107,
            physicalMeter: {
              id: 1,
            },
          },
        };

        const graphContents = mapNormalizedPaginatedResultToGraphData(sameUnit);

        expect(graphContents.axes.left).toEqual('mW');
      });

      it('extracts two axes if measurements are of exactly two different units', () => {
        const twoDifferentUnits: ObjectsById<Measurement> = {
          1: {
            id: 1,
            quantity: 'Power',
            value: 0.4353763591158477,
            unit: 'mW',
            created: 1516521585107,
            physicalMeter: {id: 1},
          },
          10: {
            id: 10,
            quantity: 'Current',
            value: 0.22359517968389853,
            unit: 'mA',
            created: 1516529685107,
            physicalMeter: {id: 1},
          },
        };

        const graphContents = mapNormalizedPaginatedResultToGraphData(twoDifferentUnits);

        expect(graphContents.axes.left).toEqual('mW');
        expect(graphContents.axes.right).toEqual('mA');
      });

      it('ignores all measurements of a third unit, if there already are two', () => {
        const threeDifferentUnits: ObjectsById<Measurement> = {
          1: {
            id: 1,
            quantity: 'Power',
            value: 0.4353763591158477,
            unit: 'mW',
            created: 1516521585107,
            physicalMeter: {id: 1},
          },
          10: {
            id: 10,
            quantity: 'Current',
            value: 0.22359517968389853,
            unit: 'mA',
            created: 1516529685107,
            physicalMeter: {id: 1},
          },
          11: {
            id: 10,
            quantity: 'Temperature inside',
            value: 0.22359517968389853,
            unit: 'C',
            created: 1516529685107,
            physicalMeter: {id: 1},
          },
        };

        const graphContents = mapNormalizedPaginatedResultToGraphData(threeDifferentUnits);

        expect(graphContents.axes.left).toEqual('mW');
        expect(graphContents.axes.right).toEqual('mA');
      });
    });
  });
});

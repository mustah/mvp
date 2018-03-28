import {GraphContents} from '../../../../../usecases/report/reportModels';
import {mapApiResponseToGraphData} from '../measurementActions';
import {MeasurementApiResponse} from '../measurementModels';

describe('measurementActions', () => {
  describe('mapApiResponseToGraphData', () => {
    describe('formats data for Rechart\'s LineGraph', () => {
      const emptyGraphContents = (): GraphContents => ({
        axes: {
          left: undefined,
          right: undefined,
        },
        data: [],
        legend: [],
        lines: [],
      });

      it('handles 0 entities gracefully', () => {
        const graphDataFromZeroEntities = mapApiResponseToGraphData({
          measurement: [],
          average: [],
        });
        expect(graphDataFromZeroEntities).toEqual(emptyGraphContents());
      });
    });

    describe('axes', () => {
      it('extracts a single axis if all measurements are of the same unit', () => {
        const sameUnit: MeasurementApiResponse = [
          {
            quantity: 'Power',
            values: [
              {
                when: 1516521585107,
                value: 0.4353763591158477,
              },
            ],
            label: '1',
            unit: 'mW',
          },
          {
            quantity: 'Power',
            values: [
              {
                when: 1516521585107,
                value: 0.4353763591158477,
              },
            ],
            label: '2',
            unit: 'mW',
          },
        ];

        const graphContents = mapApiResponseToGraphData({
          measurement: sameUnit,
          average: [],
        });

        expect(graphContents.axes.left).toEqual('mW');
      });

      it('extracts two axes if measurements are of exactly two different units', () => {
        const twoDifferentUnits: MeasurementApiResponse = [
          {
            quantity: 'Power',
            values: [
              {
                when: 1516521585107,
                value: 0.4353763591158477,
              },
            ],
            label: '1',
            unit: 'mW',
          },
          {
            quantity: 'Current',
            values: [
              {
                when: 1516521585107,
                value: 0.4353763591158477,
              },
            ],
            label: '1',
            unit: 'mA',
          },
        ];

        const graphContents = mapApiResponseToGraphData({
          measurement: twoDifferentUnits,
          average: [],
        });

        expect(graphContents.axes.left).toEqual('mW');
        expect(graphContents.axes.right).toEqual('mA');
      });

      it('ignores all measurements of a third unit, if there already are two', () => {
        const threeDifferentUnits: MeasurementApiResponse = [
          {
            quantity: 'Power',
            values: [
              {
                when: 1516521585107,
                value: 0.4353763591158477,
              },
            ],
            label: '1',
            unit: 'mW',
          },
          {
            quantity: 'Current',
            values: [
              {
                when: 1516521585107,
                value: 0.4353763591158477,
              },
            ],
            label: '1',
            unit: 'mA',
          },
          {
            quantity: 'Temperature inside',
            values: [
              {
                when: 1516521585107,
                value: 0.4353763591158477,
              },
            ],
            label: '1',
            unit: 'C',
          },
        ];

        const graphContents = mapApiResponseToGraphData({
          measurement: threeDifferentUnits,
          average: [],
        });

        expect(graphContents.axes.left).toEqual('mW');
        expect(graphContents.axes.right).toEqual('mA');
      });
    });
  });
});

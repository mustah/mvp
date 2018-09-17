import {GraphContents} from '../../../../../../usecases/report/reportModels';
import {MeasurementApiResponse, MeasurementResponses, Quantity} from '../../measurementModels';
import {mapApiResponseToGraphData} from '../apiResponseToGraphContents';

describe('apiResponseToGraphContents', () => {

  const emptyResponses = (): MeasurementResponses => ({
    measurement: [],
    average: [],
    cities: [],
  });

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
        const graphDataFromZeroEntities = mapApiResponseToGraphData(emptyResponses());
        expect(graphDataFromZeroEntities).toEqual(emptyGraphContents());
      });
    });

    describe('axes', () => {
      it('extracts a single axis if all measurements are of the same unit', () => {
        const sameUnit: MeasurementApiResponse = [
          {
            id: 'meter a',
            city: 'Varberg',
            address: 'Drottningatan 1',
            quantity: Quantity.power,
            medium: 'Electricity',
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
            id: 'meter b',
            city: 'Varberg',
            address: 'Drottningatan 1',
            quantity: Quantity.power,
            medium: 'Electricity',
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
          ...emptyResponses(),
          measurement: sameUnit,
        });

        expect(graphContents.axes.left).toEqual('mW');
      });

      it('extracts two axes if measurements are of exactly two different units', () => {
        const twoDifferentUnits: MeasurementApiResponse = [
          {
            id: 'meter a',
            city: 'Varberg',
            address: 'Drottningatan 1',
            quantity: Quantity.power,
            medium: 'Electricity',
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
            id: 'meter b',
            city: 'Varberg',
            address: 'Drottningatan 1',
            quantity: Quantity.forwardTemperature,
            medium: 'Electricity',
            values: [
              {
                when: 1516521585107,
                value: 0.4353763591158477,
              },
            ],
            label: '1',
            unit: '°C',
          },
        ];

        const graphContents = mapApiResponseToGraphData({
          ...emptyResponses(),
          measurement: twoDifferentUnits,
        });

        expect(graphContents.axes.left).toEqual('mW');
        expect(graphContents.axes.right).toEqual('°C');
      });

      it('ignores all measurements of a third unit, if there already are two', () => {
        const threeDifferentUnits: MeasurementApiResponse = [
          {
            id: 'meter a',
            city: 'Varberg',
            address: 'Drottningatan 1',
            quantity: Quantity.power,
            medium: 'Electricity',
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
            id: 'meter b',
            city: 'Varberg',
            address: 'Drottningatan 1',
            quantity: Quantity.energy,
            medium: 'Electricity',
            values: [
              {
                when: 1516521585107,
                value: 0.4353763591158477,
              },
            ],
            label: '1',
            unit: 'kWh',
          },
          {
            id: 'meter c',
            city: 'Varberg',
            address: 'Västgötagatan 10',
            quantity: Quantity.differenceTemperature,
            medium: 'Electricity',
            values: [
              {
                when: 1516521585107,
                value: 0.4353763591158477,
              },
            ],
            label: '1',
            unit: 'K',
          },
        ];

        const graphContents = mapApiResponseToGraphData({
          ...emptyResponses(),
          measurement: threeDifferentUnits,
        });

        expect(graphContents.axes.left).toEqual('mW');
        expect(graphContents.axes.right).toEqual('kWh');
      });

      it(
        'adjusts the starting position of the x-axis to the first measurement, not average',
        () => {
          const firstMeasurement: number = 1516521585107;
          const slightlyLaterThanFirstAverage: MeasurementApiResponse = [
            {
              id: 'meter a',
              city: 'Varberg',
              address: 'Drottningatan 1',
              quantity: Quantity.power,
              medium: 'Electricity',
              values: [
                {
                  when: firstMeasurement,
                  value: 0.4353763591158477,
                },
              ],
              label: 'meter',
              unit: 'mW',
            },
          ];

          const average: MeasurementApiResponse = [
            {
              id: 'meter a',
              city: 'Varberg',
              address: 'Drottningatan 1',
              quantity: Quantity.power,
              medium: 'Electricity',
              values: [
                {
                  when: firstMeasurement - 10,
                  value: 111,
                },
              ],
              label: 'average',
              unit: 'mW',
            },
            {
              id: 'meter b',
              city: 'Varberg',
              address: 'Drottningatan 1',
              quantity: Quantity.power,
              medium: 'Electricity',
              values: [
                {
                  when: firstMeasurement + 10,
                  value: 222,
                },
              ],
              label: 'average',
              unit: 'mW',
            },
          ];

          const graphContents = mapApiResponseToGraphData({
            ...emptyResponses(),
            measurement: slightlyLaterThanFirstAverage,
            average,
          });

          expect(graphContents.data).toHaveLength(2);
          expect(graphContents
            .data
            .filter(
              (value: {[key: string]: number}) => value.name >= firstMeasurement)
            .length,
          ).toEqual(2);
        },
      );

    });

  });


});

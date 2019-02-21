import {GraphContents} from '../../../../../usecases/report/reportModels';
import {toGraphContents} from '../graphContentsMapper';
import {
  MeasurementApiResponse,
  MeasurementResponse,
  MeasurementResponsePart,
  MeasurementValues,
  Quantity
} from '../measurementModels';
import {hasMeasurements} from '../measurementSelectors';

describe('measurementSelectors', () => {

  const emptyResponses: MeasurementResponse = {measurements: [], average: []};

  const values: MeasurementValues = [
    {
      when: 1516521585107,
      value: 0.4353763591158477,
    },
  ];

  const responsePart: MeasurementResponsePart = {
    id: 'meter a',
    city: 'Varberg',
    address: 'Drottningatan 1',
    quantity: Quantity.power,
    medium: 'Electricity',
    values,
    label: '1',
    unit: 'mW',
  };

  const measurementResponse: MeasurementResponse = {
    measurements: [responsePart],
    average: [],
  };

  describe('toGraphContents', () => {
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
        const graphDataFromZeroEntities: GraphContents = toGraphContents(emptyResponses);
        expect(graphDataFromZeroEntities).toEqual(emptyGraphContents());
      });
    });

    describe('hasMeasurements', () => {
      it('returns false when no measurements in response', () => {
        expect(hasMeasurements(emptyResponses)).toBe(false);
      });

      it('returns false when measurements has no values', () => {
        const response: MeasurementResponse = {
          ...measurementResponse,
          measurements: [{...responsePart, values: []}],
        };
        expect(hasMeasurements(response)).toBe(false);
      });

      it('returns true when measurements has points with values', () => {
        expect(hasMeasurements(measurementResponse)).toBe(true);
      });

      it('returns false when measurements has points, but no values', () => {
        const response: MeasurementResponse = {
          ...measurementResponse,
          measurements: [{...responsePart, values: [{when: 1516521585107}]}],
        };
        expect(hasMeasurements(response)).toBe(false);
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

        const graphContents: GraphContents = toGraphContents({...emptyResponses, measurements: sameUnit});

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

        const graphContents: GraphContents = toGraphContents({
          ...emptyResponses,
          measurements: twoDifferentUnits,
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

        const graphContents: GraphContents = toGraphContents({
          ...emptyResponses,
          measurements: threeDifferentUnits,
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

          const graphContents: GraphContents = toGraphContents({
            ...emptyResponses,
            measurements: slightlyLaterThanFirstAverage,
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

})
;

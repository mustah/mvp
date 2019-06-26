import {initTranslations} from '../../../../../i18n/__tests__/i18nMock';
import {Dictionary} from '../../../../../types/Types';
import {toGraphContents} from '../../../../../usecases/report/helpers/lineChartHelper';
import {GraphContents} from '../../../../report/reportModels';
import {
  getMediumType,
  MeasurementResponse,
  MeasurementResponsePart,
  MeasurementsApiResponse,
  MeasurementValue,
  Medium,
  Quantity,
  TooltipMeta
} from '../measurementModels';
import {hasMeasurementValues} from '../measurementSelectors';

describe('measurementSelectors', () => {

  initTranslations({
    code: 'en',
    translation: {
      average: 'medelvärde',
    },
  });

  const emptyResponses: MeasurementResponse = {measurements: [], average: [], compare: []};

  const values: MeasurementValue[] = [
    {
      when: 1_516_521_585_107,
      value: 0.4353,
    },
  ];

  const responseItem: MeasurementResponsePart = {
    id: '1',
    label: 'a',
    medium: getMediumType(Medium.electricity),
    quantity: Quantity.power,
    values,
    unit: 'mW',
  };

  const makeResponseItem = (item: Partial<MeasurementResponsePart>): MeasurementResponsePart => ({
    ...responseItem,
    ...item
  });

  const selectedQuantities = [Quantity.power];

  const measurementResponse: MeasurementResponse = {
    measurements: [responseItem],
    average: [],
    compare: [],
  };

  describe('toGraphContents', () => {

    describe('formats data for Rechart\'s LineGraph', () => {
      const emptyGraphContents = (): GraphContents => ({
        axes: {},
        data: [],
        legend: [],
        lines: [],
      });

      it('handles 0 entities gracefully', () => {
        expect(toGraphContents(emptyResponses, [])).toEqual(emptyGraphContents());
      });
    });

    describe('hasMeasurementValues', () => {

      it('returns false when no measurements in response', () => {
        expect(hasMeasurementValues(emptyResponses)).toBe(false);
      });

      it('returns false when measurements has no values', () => {
        const response: MeasurementResponse = {
          ...measurementResponse,
          measurements: [{...responseItem, values: []}],
        };
        expect(hasMeasurementValues(response)).toBe(false);
      });

      it('returns true when measurements has points with values', () => {
        expect(hasMeasurementValues(measurementResponse)).toBe(true);
      });

      it('returns false when measurements has points, but no values', () => {
        const response: MeasurementResponse = {
          ...measurementResponse,
          measurements: [{...responseItem, values: [{when: 1516521585107}]}],
        };
        expect(hasMeasurementValues(response)).toBe(false);
      });

      it('has measurements where there are average responses', () => {
        const response: MeasurementResponse = {
          measurements: [],
          compare: [],
          average: [responseItem]
        };
        expect(hasMeasurementValues(response)).toBe(true);
      });

    });

    describe('axes', () => {

      it('extracts a single axis if all measurements are of the same unit', () => {
        const sameUnit: MeasurementsApiResponse = [responseItem, responseItem];

        const {axes: {left, right}} = toGraphContents({...emptyResponses, measurements: sameUnit}, selectedQuantities);

        expect(left).toEqual('mW');
        expect(right).toBeUndefined();
      });

      it('extracts two axes if measurements are of exactly two different units', () => {
        const twoDifferentUnits: MeasurementsApiResponse = [
          responseItem,
          makeResponseItem({id: 'meter b', quantity: Quantity.forwardTemperature, unit: '°C'}),
        ];

        const graphContents: GraphContents = toGraphContents({
          ...emptyResponses,
          measurements: twoDifferentUnits,
        }, [...selectedQuantities, Quantity.forwardTemperature]);

        expect(graphContents.axes.left).toEqual('mW');
        expect(graphContents.axes.right).toEqual('°C');
      });

      it('ignores all measurements of a third unit, if there already are two', () => {
        const threeDifferentUnits: MeasurementsApiResponse = [
          responseItem,
          makeResponseItem({id: 'b', unit: 'kWh'}),
          makeResponseItem({id: 'c', unit: 'K'}),
        ];

        const graphContents: GraphContents = toGraphContents({
          ...emptyResponses,
          measurements: threeDifferentUnits,
        }, selectedQuantities);

        expect(graphContents.axes.left).toEqual('mW');
        expect(graphContents.axes.right).toEqual('kWh');
      });

      it('adjusts the starting position of the x-axis to the first measurement of any of the responses', () => {
          const startTimestamp: number = 1516521585107;
          const slightlyLaterThanFirstAverage: MeasurementsApiResponse = [
            makeResponseItem({values: [{when: startTimestamp, value: 0.4353}]}),
          ];

          const average: MeasurementsApiResponse = [
            makeResponseItem({values: [{when: startTimestamp - 10, value: 111}]}),
            makeResponseItem({values: [{when: startTimestamp + 10, value: 222}]}),
          ];

          const graphContents: GraphContents = toGraphContents({
            measurements: slightlyLaterThanFirstAverage,
            average,
            compare: [],
          }, selectedQuantities);

          expect(graphContents.data).toHaveLength(3);
          expect(graphContents.data.filter((it: Dictionary<number>) => it.name >= startTimestamp).length).toEqual(3);
        },
      );

    });

    describe('legend', () => {

      it('has no legend data', () => {
        const {legend}: GraphContents = toGraphContents(emptyResponses, []);

        expect(legend).toEqual([]);
      });

      it('has legend items for measurement quantities', () => {
        const {legend}: GraphContents = toGraphContents({
          measurements: [responseItem],
          average: [], compare: []
        }, selectedQuantities);
        expect(legend).toEqual([{type: 'line', color: '#00B0FF', value: 'Power'}]);
      });

      it('has only one legend item for each quantity', () => {
        const {legend}: GraphContents = toGraphContents({
          measurements: [responseItem, makeResponseItem({id: 'b'})],
          average: [],
          compare: [],
        }, selectedQuantities);

        expect(legend).toEqual([{type: 'line', color: '#00B0FF', value: 'Power'}]);
      });

      it('has legend items for average response too', () => {
        const {legend}: GraphContents = toGraphContents({
          measurements: [responseItem],
          average: [makeResponseItem({id: 'b'})],
          compare: [],
        }, selectedQuantities);

        expect(legend).toEqual([
          {type: 'line', color: '#00B0FF', value: 'Power'},
          {type: 'line', color: '#00B0FF', value: 'Medelvärde Power'},
        ]);
      });
    });

    describe('data', () => {

      it('has data for measurements', () => {
        const {data}: GraphContents = toGraphContents({
          measurements: [responseItem],
          average: [],
          compare: [],
        }, selectedQuantities);

        const expected: Array<Dictionary<number | TooltipMeta>> = [
          {
            'name': 1_516_521_585_107_000,
            'measurement-Power-a-1': 0.4353,
            'measurement-Power-a-1-timestamp': NaN,
            'measurement-Power-a-1-meta': {id: '1', quantity: Quantity.power},
          },
        ];
        expect(data).toEqual(expected);
      });

      it('has data for average', () => {
        const {data}: GraphContents = toGraphContents({
          average: [makeResponseItem({id: '2', quantity: Quantity.energy})],
          compare: [],
          measurements: [],
        }, [...selectedQuantities, Quantity.energy]);

        const expected: Array<Dictionary<number | TooltipMeta>> = [
          {
            'name': 1_516_521_585_107_000,
            'aggregate-Energy-a-2': 0.4353,
            'aggregate-Energy-a-2-timestamp': NaN,
            'aggregate-Energy-a-2-meta': {id: '2', quantity: Quantity.energy},
          },
        ];
        expect(data).toEqual(expected);
      });
    });

    describe('compare', () => {

      it('has data item for compare graph content', () => {
        const startTimestamp: number = 1_516_521_585_000;

        const measurements: MeasurementsApiResponse = [
          makeResponseItem({values: [{when: startTimestamp, value: 111}]}),
          makeResponseItem({values: [{when: startTimestamp + 1000, value: 222}], id: '2', label: 'b'}),
        ];

        const compare: MeasurementsApiResponse = [
          makeResponseItem({values: [{when: startTimestamp - 1000, value: 888}]}),
          makeResponseItem({values: [{when: startTimestamp - 2000, value: 999}], id: '2', label: 'b'}),
        ];

        const expected: Array<Dictionary<number | TooltipMeta>> = [
          {
            'name': 1_516_521_585_000_000,
            'measurement-Power-a-1': 111,
            'measurement-Power-a-1-timestamp': NaN,
            'measurement-Power-a-1-meta': {id: '1', quantity: Quantity.power},
            'compare-Power-a-1': 888,
            'compare-Power-a-1-timestamp': 1_516_521_584_000_000,
            'compare-Power-a-1-meta': {id: '1', quantity: Quantity.power},
          },
          {
            'name': 1_516_521_586_000_000,
            'measurement-Power-b-2': 222,
            'measurement-Power-b-2-timestamp': NaN,
            'measurement-Power-b-2-meta': {id: '2', quantity: Quantity.power},
            'compare-Power-b-2': 999,
            'compare-Power-b-2-timestamp': 1_516_521_583_000_000,
            'compare-Power-b-2-meta': {id: '2', quantity: Quantity.power},
          }
        ];
        expect(toGraphContents({measurements, average: [], compare}, selectedQuantities).data).toEqual(expected);
      });

      it('does not compare measurements that have not same number of measurement response results', () => {
        const startTimestamp: number = 1_516_521_585_000;

        const measurements: MeasurementsApiResponse = [
          makeResponseItem({values: [{when: startTimestamp, value: 111}]}),
        ];

        const responseItemWillNotBeCompared = makeResponseItem({
          values: [{when: startTimestamp - 2000, value: 222}],
          id: '2',
          label: 'b'
        });

        const compare: MeasurementsApiResponse = [
          makeResponseItem({values: [{when: startTimestamp - 1000, value: 888}]}),
          responseItemWillNotBeCompared,
        ];

        const expected: Array<Dictionary<number | TooltipMeta>> = [
          {
            'name': 1_516_521_585_000_000,
            'measurement-Power-a-1': 111,
            'measurement-Power-a-1-timestamp': NaN,
            'measurement-Power-a-1-meta': {id: '1', quantity: Quantity.power},
            'compare-Power-a-1': 888,
            'compare-Power-a-1-timestamp': 1_516_521_584_000_000,
            'compare-Power-a-1-meta': {id: '1', quantity: Quantity.power},
          }
        ];

        expect(toGraphContents({measurements, average: [], compare}, selectedQuantities).data).toEqual(expected);
      });
    });

  });

});

import axios from 'axios';
import {Period} from '../../../../../components/dates/dateModels';
import {Maybe} from '../../../../../helpers/Maybe';
import {authenticate} from '../../../../../services/restClient';
import {GraphContents} from '../../../../../usecases/report/reportModels';
import {fetchMeasurements, mapApiResponseToGraphData} from '../measurementActions';
import {AverageApiResponse, MeasurementApiResponse, MeasurementResponses} from '../measurementModels';
import MockAdapter = require('axios-mock-adapter');

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

      it('adjusts the starting position of the x-axis to the first measurement, not average', () => {
        const firstMeasurement: number = 1516521585107;
        const slightlyLaterThanFirstAverage: MeasurementApiResponse = [
          {
            quantity: 'Power',
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

        const average: AverageApiResponse = [
          {
            quantity: 'Power',
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
            quantity: 'Power',
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
      });

    });

  });

  describe('fetchMeasurements', () => {
    const emptyResponses: MeasurementResponses = Object.freeze({
      measurement: [],
      average: [],
    });

    it('returns empty data if no quantities are not provided', async () => {
      const response = await fetchMeasurements([], ['123abc'], Period.currentMonth, Maybe.nothing());
      expect(response).toEqual(emptyResponses);
    });

    it('returns empty data if no meter ids are provided', async () => {
      const response = await fetchMeasurements(['Power'], [], Period.currentMonth, Maybe.nothing());
      expect(response).toEqual(emptyResponses);
    });

    it('does not include average endpoint when asking for measurements for single meter', async () => {
      const mockRestClient = new MockAdapter(axios);
      authenticate('test');

      const requestedUrls: string[] = [];
      mockRestClient.onGet().reply((config) => {
        requestedUrls.push(config.url);
        return [200, 'some data'];
      });

      await fetchMeasurements(['Power'], ['123abc'], Period.currentMonth, Maybe.nothing());
      expect(requestedUrls.length).toEqual(1);
      expect(requestedUrls[0]).toMatch(/\/measurements\?quantities=Power&meters=123abc&after=20.+Z&before=20.+Z/);
    });

    it('includes average when asking for measurements for multiple meters', async () => {
      const mockRestClient = new MockAdapter(axios);
      authenticate('test');

      const requestedUrls: string[] = [];
      mockRestClient.onGet().reply((config) => {
        requestedUrls.push(config.url);
        return [200, 'some data'];
      });

      await fetchMeasurements(['Power'], ['123abc', '345def', '456ghi'], Period.currentMonth, Maybe.nothing());
      expect(requestedUrls.length).toEqual(2);
      expect(requestedUrls[0]).toMatch(
        /\/measurements\/average\?quantities=Power&meters=123abc,345def,456ghi&after=20.+Z&before=20.+Z/);
      expect(requestedUrls[1]).toMatch(
        /\/measurements\?quantities=Power&meters=123abc,345def,456ghi&after=20.+Z&before=20.+Z/);
    });

    it('provides a result suitable for parsing by mapApiResponseToGraphData', async () => {
      const mockRestClient = new MockAdapter(axios);
      authenticate('test');

      const requestedUrls: string[] = [];

      mockRestClient.onGet().reply(async (config) => {
        requestedUrls.push(config.url);

        const measurement: MeasurementApiResponse = [
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

        const average: AverageApiResponse = [
          {
            quantity: 'Power',
            unit: 'mW',
            label: 'average',
            values: [
              {
                when: 1516521585107,
                value: 0.44,
              },
              {
                when: 1516521585109,
                value: 0.55,
              },
            ],
          },
        ];

        if (config.url.match(/^\/measurements\/average/)) {
          return [200, average];
        } else {
          return [200, measurement];
        }
      });

      const responses =
        await fetchMeasurements(['Power'], ['123abc', '345def', '456ghi'], Period.currentMonth, Maybe.nothing());
      expect(requestedUrls.length).toEqual(2);

      const graphData = mapApiResponseToGraphData(responses);
      expect(graphData.axes.left).toEqual('mW');
      expect(graphData.data).toHaveLength(2);
      expect(graphData.lines).toHaveLength(3);
    });
  });
});

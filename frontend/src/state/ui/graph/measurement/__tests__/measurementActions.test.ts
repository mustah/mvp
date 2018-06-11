import axios from 'axios';
import {Period} from '../../../../../components/dates/dateModels';
import {Medium} from '../../../../../components/indicators/indicatorWidgetModels';
import {Maybe} from '../../../../../helpers/Maybe';
import {initTranslations} from '../../../../../i18n/__tests__/i18nMock';
import {authenticate} from '../../../../../services/restClient';
import {Unauthorized} from '../../../../../usecases/auth/authModels';
import {GraphContainerState} from '../../../../../usecases/report/containers/GraphContainer';
import {GraphContents} from '../../../../../usecases/report/reportModels';
import {fetchMeasurements, mapApiResponseToGraphData} from '../measurementActions';
import {initialState, MeasurementApiResponse, Quantity} from '../measurementModels';
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
            quantity: Quantity.power,
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
            quantity: Quantity.power,
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
            quantity: Quantity.power,
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
            quantity: Quantity.forwardTemperature,
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
          measurement: twoDifferentUnits,
          average: [],
        });

        expect(graphContents.axes.left).toEqual('mW');
        expect(graphContents.axes.right).toEqual('°C');
      });

      it('ignores all measurements of a third unit, if there already are two', () => {
        const threeDifferentUnits: MeasurementApiResponse = [
          {
            quantity: Quantity.power,
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
            quantity: Quantity.energy,
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
            quantity: Quantity.differenceTemperature,
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
          measurement: threeDifferentUnits,
          average: [],
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
              quantity: Quantity.power,
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
              quantity: Quantity.power,
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
              quantity: Quantity.power,
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
        },
      );

    });

  });

  describe('fetchMeasurements', () => {

    initTranslations({
      code: 'en',
      translation: {
        test: 'no translations will default to key',
      },
    });

    let state: GraphContainerState;
    let loggedOut: string;
    const updateState = (updatedState: GraphContainerState) => state = {...updatedState};
    const logout = (error?: Unauthorized) => error ? loggedOut = error.message : 'logged out';

    beforeEach(() => {
      state = initialState;
      loggedOut = 'not logged out';
    });

    it('sets default state if no quantities are provided', async () => {
      updateState({...initialState, isFetching: true});
      const fetching: GraphContainerState = {...initialState};
      expect(state).not.toEqual(fetching);

      await fetchMeasurements(
        [],
        [],
        ['123abc'],
        Period.currentMonth,
        Maybe.nothing(),
        updateState,
        logout,
      );
      const expected: GraphContainerState = {...initialState};
      expect(state).toEqual(expected);
    });

    it('includes meters and excludes cities/clusters/addreses in request', async () => {
      const mockRestClient = new MockAdapter(axios);
      authenticate('test');

      const requestedUrls: string[] = [];
      mockRestClient.onGet().reply((config) => {
        requestedUrls.push(config.url);
        return [200, 'some data'];
      });

      await fetchMeasurements(
        [Medium.districtHeating],
        [Quantity.power],
        ['sweden,höganäs,hasselgatan 4', '8c5584ca-eaa3-4199-bf85-871edba8945e'],
        Period.currentMonth,
        Maybe.nothing(),
        (state: GraphContainerState) => void(0),
        (error?: Unauthorized) => void(0),
      );

      expect(requestedUrls[0]).toMatch(
        /\/measurements\?quantities=Power&meters=8c5584ca-eaa3-4199-bf85-871edba8945e&after=20.+Z&before=20.+Z/);
    });

    it('returns empty data if no meter ids are provided', async () => {
      updateState({...initialState, isFetching: true});
      const fetching: GraphContainerState = {...initialState};
      expect(state).not.toEqual(fetching);

      await fetchMeasurements(
        [Medium.districtHeating],
        [Quantity.power],
        [],
        Period.currentMonth,
        Maybe.nothing(),
        updateState,
        logout,
      );
      const expected: GraphContainerState = {...initialState};
      expect(state).toEqual(expected);
    });

    it(
      'does not include average endpoint when asking for measurements for single meter',
      async () => {
        const mockRestClient = new MockAdapter(axios);
        authenticate('test');

        const requestedUrls: string[] = [];
        mockRestClient.onGet().reply((config) => {
          requestedUrls.push(config.url);
          return [200, 'some data'];
        });

        await fetchMeasurements(
          [Medium.districtHeating],
          [Quantity.power],
          ['123abc'],
          Period.currentMonth,
          Maybe.nothing(),
          updateState,
          logout,
        );
        expect(requestedUrls.length).toEqual(1);
        expect(requestedUrls[0]).toMatch(
          /\/measurements\?quantities=Power&meters=123abc&after=20.+Z&before=20.+Z/);
      },
    );

    it('includes average when asking for measurements for multiple meters', async () => {
      const mockRestClient = new MockAdapter(axios);
      authenticate('test');

      const requestedUrls: string[] = [];
      mockRestClient.onGet().reply((config) => {
        requestedUrls.push(config.url);
        return [200, 'some data'];
      });

      await fetchMeasurements(
        [Medium.districtHeating],
        [Quantity.power],
        ['123abc', '345def', '456ghi'],
        Period.currentMonth,
        Maybe.nothing(),
        updateState,
        logout,
      );
      expect(requestedUrls).toHaveProperty('length', 2);
      requestedUrls.sort();
      expect(requestedUrls[0])
        .toMatch(
          /\/measurements\/average\?quantities=Power&meters=123abc,345def,456ghi&after=20.+Z&before=20.+Z/);
      expect(requestedUrls[1])
        .toMatch(
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
            quantity: Quantity.power,
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
            quantity: Quantity.power,
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

        const average: MeasurementApiResponse = [
          {
            quantity: Quantity.power,
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

      await fetchMeasurements(
        [Medium.districtHeating],
        [Quantity.power],
        ['123abc', '345def', '456ghi'],
        Period.currentMonth,
        Maybe.nothing(),
        updateState,
        logout,
      );

      expect(requestedUrls.length).toEqual(2);
      expect(state.graphContents.axes.left).toEqual('mW');
      expect(state.graphContents.data).toHaveLength(2);
      expect(state.graphContents.lines).toHaveLength(3);
    });

    it('filters out average readouts without values', async () => {
      const mockRestClient = new MockAdapter(axios);
      authenticate('test');

      const requestedUrls: string[] = [];

      mockRestClient.onGet().reply(async (config) => {
        requestedUrls.push(config.url);

        const measurement: MeasurementApiResponse = [
          {
            quantity: Quantity.power,
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
            quantity: Quantity.power,
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

        const average: MeasurementApiResponse = [
          {
            quantity: Quantity.power,
            unit: 'mW',
            label: 'average',
            values: [
              {
                when: 1516521585107,
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

      await fetchMeasurements(
        [Medium.districtHeating],
        [Quantity.power],
        ['123abc', '345def', '456ghi'],
        Period.currentMonth,
        Maybe.nothing(),
        updateState,
        logout,
      );

      const timestampsWithAverageValues = state.graphContents.data
        .filter((pointInTime) => Object.keys(pointInTime).includes('Average Power'));
      expect(timestampsWithAverageValues).toHaveLength(1);
    });

    it('keeps average readouts with a value of 0', async () => {
      const mockRestClient = new MockAdapter(axios);
      authenticate('test');

      const requestedUrls: string[] = [];

      mockRestClient.onGet().reply(async (config) => {
        requestedUrls.push(config.url);

        const measurement: MeasurementApiResponse = [
          {
            quantity: Quantity.power,
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
            quantity: Quantity.power,
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

        const average: MeasurementApiResponse = [
          {
            quantity: Quantity.power,
            unit: 'mW',
            label: 'average',
            values: [
              {
                when: 1516521585107,
                value: 0.0,
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

      await fetchMeasurements(
        [Medium.districtHeating],
        [Quantity.power],
        ['123abc', '345def', '456ghi'],
        Period.currentMonth,
        Maybe.nothing(),
        updateState,
        logout,
      );

      const timestampsWithAverageValues = state.graphContents.data
        .filter((pointInTime) => Object.keys(pointInTime).includes('Average Power'));
      expect(timestampsWithAverageValues).toHaveLength(2);
    });
  });
});

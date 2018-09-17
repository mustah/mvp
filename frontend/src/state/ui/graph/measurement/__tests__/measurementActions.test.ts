import axios from 'axios';
import {Period} from '../../../../../components/dates/dateModels';
import {Medium} from '../../../../../components/indicators/indicatorWidgetModels';
import {Maybe} from '../../../../../helpers/Maybe';
import {initTranslations} from '../../../../../i18n/__tests__/i18nMock';
import {authenticate} from '../../../../../services/restClient';
import {Unauthorized} from '../../../../../usecases/auth/authModels';
import {ReportContainerState} from '../../../../../usecases/report/containers/ReportContainer';
import {GraphContents} from '../../../../../usecases/report/reportModels';
import {fetchMeasurements, mapApiResponseToGraphData} from '../measurementActions';
import {initialState, MeasurementApiResponse, MeasurementResponses, Quantity} from '../measurementModels';
import MockAdapter = require('axios-mock-adapter');

describe('measurementActions', () => {

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

  describe('fetchMeasurements', () => {

    initTranslations({
      code: 'en',
      translation: {
        test: 'no translations will default to key',
      },
    });

    let state: ReportContainerState;
    let loggedOut: string;
    const updateState = (updatedState: ReportContainerState) => state = {...updatedState};
    const logout = (error?: Unauthorized) => error ? loggedOut = error.message : 'logged out';

    beforeEach(() => {
      state = initialState;
      loggedOut = 'not logged out';
    });

    it('sets default state if no quantities are provided', async () => {
      updateState({...initialState, isFetching: true});
      const fetching: ReportContainerState = {...initialState};
      expect(state).not.toEqual(fetching);

      await fetchMeasurements({
        selectedIndicators: [],
        quantities: [],
        selectedListItems: ['123abc'],
        timePeriod: Period.currentMonth,
        customDateRange: Maybe.nothing(),
        updateState,
        logout,
      });
      const expected: ReportContainerState = {...initialState};
      expect(state).toEqual(expected);
    });

    it('includes meters and excludes clusters/addreses in request', async () => {
      const mockRestClient = new MockAdapter(axios);
      authenticate('test');

      const requestedUrls: string[] = [];
      mockRestClient.onGet().reply((config) => {
        requestedUrls.push(config.url);
        return [200, 'some data'];
      });

      await fetchMeasurements({
        selectedIndicators: [Medium.districtHeating],
        quantities: [Quantity.power],
        selectedListItems: ['sweden,höganäs,hasselgatan 4', '8c5584ca-eaa3-4199-bf85-871edba8945e'],
        timePeriod: Period.currentMonth,
        customDateRange: Maybe.nothing(),
        updateState: (state: ReportContainerState) => void(0),
        logout: (error?: Unauthorized) => void(0),
      });

      expect(requestedUrls[0]).toMatch(
        /\/measurements\?quantities=Power&meters=8c5584ca-eaa3-4199-bf85-871edba8945e&after=20.+Z&before=20.+Z/);
    });

    describe('cities', () => {

      it('requests cities when no meters are selected', async () => {
        const mockRestClient = new MockAdapter(axios);
        authenticate('test');

        const requestedUrls: string[] = [];
        mockRestClient.onGet().reply((config) => {
          requestedUrls.push(config.url);
          return [200, 'some data'];
        });

        await fetchMeasurements({
          selectedIndicators: [Medium.districtHeating],
          quantities: [Quantity.power],
          selectedListItems: ['sweden,höganäs', 'sweden,göteborg'],
          timePeriod: Period.currentMonth,
          customDateRange: Maybe.nothing(),
          updateState: (state: ReportContainerState) => void(0),
          logout: (error?: Unauthorized) => void(0),
        });

        expect(requestedUrls).toHaveLength(1);

        const expected: RegExp =
          /\/measurements\/cities\?quantities=Power&city=sweden,höganäs&city=sweden,göteborg&after=20.+Z&before=20.+Z/;
        expect(requestedUrls[0]).toMatch(expected);
      });

      it('requests cities when meters are selected too', async () => {
        const mockRestClient = new MockAdapter(axios);
        authenticate('test');

        const requestedUrls: string[] = [];
        mockRestClient.onGet().reply((config) => {
          requestedUrls.push(config.url);
          return [200, 'some data'];
        });

        await fetchMeasurements({
          selectedIndicators: [Medium.districtHeating],
          quantities: [Quantity.power],
          selectedListItems: ['sweden,höganäs', '8c5584ca-eaa3-4199-bf85-871edba8945e'],
          timePeriod: Period.currentMonth,
          customDateRange: Maybe.nothing(),
          updateState: (state: ReportContainerState) => void(0),
          logout: (error?: Unauthorized) => void(0),
        });

        expect(requestedUrls).toHaveLength(2);

        const meterUrl: RegExp =
          /\/measurements\?quantities=Power&meters=8c5584ca-eaa3-4199-bf85-871edba8945e&after=20.+Z&before=20.+Z/;
        expect(requestedUrls[0]).toMatch(meterUrl);

        const cityUrl: RegExp =
          /\/measurements\/cities\?quantities=Power&city=sweden,höganäs&after=20.+Z&before=20.+Z/;
        expect(requestedUrls[1]).toMatch(cityUrl);
      });

      it('does not request addresses against cities endpoint', async () => {
        const mockRestClient = new MockAdapter(axios);
        authenticate('test');

        const requestedUrls: string[] = [];
        mockRestClient.onGet().reply((config) => {
          requestedUrls.push(config.url);
          return [200, 'some data'];
        });

        await fetchMeasurements({
          selectedIndicators: [Medium.districtHeating],
          quantities: [Quantity.power],
          selectedListItems: ['sweden,höganäs', 'sweden,höganäs,hasselgatan 4'],
          timePeriod: Period.currentMonth,
          customDateRange: Maybe.nothing(),
          updateState: (state: ReportContainerState) => void(0),
          logout: (error?: Unauthorized) => void(0),
        });

        expect(requestedUrls).toHaveLength(1);

        const cityUrl: RegExp =
          /\/measurements\/cities\?quantities=Power&city=sweden,höganäs&after=20.+Z&before=20.+Z/;
        expect(requestedUrls[0]).toMatch(cityUrl);
      });

    });

    it('returns empty data if no meter ids are provided', async () => {
      updateState({...initialState, isFetching: true});
      const fetching: ReportContainerState = {...initialState};
      expect(state).not.toEqual(fetching);

      await fetchMeasurements({
        selectedIndicators: [Medium.districtHeating],
        quantities: [Quantity.power],
        selectedListItems: [],
        timePeriod: Period.currentMonth,
        customDateRange: Maybe.nothing(),
        updateState,
        logout,
      });
      const expected: ReportContainerState = {...initialState};
      expect(state).toEqual(expected);
    });

    it('does not include average endpoint when asking for measurements for single meter', async () => {
        const mockRestClient = new MockAdapter(axios);
        authenticate('test');

        const requestedUrls: string[] = [];
        mockRestClient.onGet().reply((config) => {
          requestedUrls.push(config.url);
          return [200, 'some data'];
        });

        await fetchMeasurements({
          selectedIndicators: [Medium.districtHeating],
          quantities: [Quantity.power],
          selectedListItems: ['123abc'],
          timePeriod: Period.currentMonth,
          customDateRange: Maybe.nothing(),
          updateState,
          logout,
        });
        expect(requestedUrls).toHaveLength(1);
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

      await fetchMeasurements({
        selectedIndicators: [Medium.districtHeating],
        quantities: [Quantity.power],
        selectedListItems: ['123abc', '345def', '456ghi'],
        timePeriod: Period.currentMonth,
        customDateRange: Maybe.nothing(),
        updateState,
        logout,
      });
      expect(requestedUrls).toHaveLength(2);
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

        const average: MeasurementApiResponse = [
          {
            id: 'Varberg',
            city: 'Varberg',
            address: '',
            quantity: Quantity.power,
            unit: 'mW',
            label: 'average',
            medium: 'Electricity',
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

      await fetchMeasurements({
        selectedIndicators: [Medium.districtHeating],
        quantities: [Quantity.power],
        selectedListItems: ['123abc', '345def', '456ghi'],
        timePeriod: Period.currentMonth,
        customDateRange: Maybe.nothing(),
        updateState,
        logout,
      });

      expect(requestedUrls).toHaveLength(2);

      const graphContents: GraphContents = mapApiResponseToGraphData(state.measurementResponse);

      expect(graphContents.axes.left).toEqual('mW');
      expect(graphContents.data).toHaveLength(2);
      expect(graphContents.lines).toHaveLength(3);
    });

    it('filters out average readouts without values', async () => {
      const mockRestClient = new MockAdapter(axios);
      authenticate('test');

      mockRestClient.onGet().reply(async (config) => {

        const measurement: MeasurementApiResponse = [
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

        const average: MeasurementApiResponse = [
          {
            id: 'Varberg',
            city: 'Varberg',
            address: '',
            quantity: Quantity.power,
            unit: 'mW',
            label: 'average',
            medium: 'Electricity',
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

      await fetchMeasurements({
        selectedIndicators: [Medium.districtHeating],
        quantities: [Quantity.power],
        selectedListItems: ['123abc', '345def', '456ghi'],
        timePeriod: Period.currentMonth,
        customDateRange: Maybe.nothing(),
        updateState,
        logout,
      });

      expect(state.measurementResponse.average[0].values).toHaveLength(1);
      expect(state.measurementResponse.average[0].values[0].value).toBe(0.55);
    });

    it('keeps average readouts with a value of 0', async () => {
      const mockRestClient = new MockAdapter(axios);
      authenticate('test');

      mockRestClient.onGet().reply(async (config) => {

        const measurement: MeasurementApiResponse = [
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

        const average: MeasurementApiResponse = [
          {
            id: 'Varberg',
            city: 'Varberg',
            address: '',
            quantity: Quantity.power,
            unit: 'mW',
            label: 'average',
            medium: 'Electricity',
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

      await fetchMeasurements({
        selectedIndicators: [Medium.districtHeating],
        quantities: [Quantity.power],
        selectedListItems: ['123abc', '345def', '456ghi'],
        timePeriod: Period.currentMonth,
        customDateRange: Maybe.nothing(),
        updateState,
        logout,
      });

      expect(state.measurementResponse.average[0].values).toHaveLength(2);
      expect(state.measurementResponse.average[0].values[0].value).toBe(0);
      expect(state.measurementResponse.average[0].values[1].value).toBe(0.55);
    });
  });
});

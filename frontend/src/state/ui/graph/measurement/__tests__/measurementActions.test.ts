import axios from 'axios';
import {default as MockAdapter} from 'axios-mock-adapter';
import {Period} from '../../../../../components/dates/dateModels';
import {Medium} from '../../../../../components/indicators/indicatorWidgetModels';
import {idGenerator} from '../../../../../helpers/idGenerator';
import {Maybe} from '../../../../../helpers/Maybe';
import {initTranslations} from '../../../../../i18n/__tests__/i18nMock';
import {authenticate} from '../../../../../services/restClient';
import {Unauthorized} from '../../../../../usecases/auth/authModels';
import {ReportContainerState} from '../../../../../usecases/report/containers/ReportContainer';
import {GraphContents} from '../../../../../usecases/report/reportModels';
import {SelectionTreeCity, SelectionTreeMeter} from '../../../../selection-tree/selectionTreeModels';
import {mapApiResponseToGraphData} from '../helpers/apiResponseToGraphContents';
import {fetchMeasurements, MeasurementOptions} from '../measurementActions';
import {initialState, MeasurementApiResponse, Quantity} from '../measurementModels';

describe('measurementActions', () => {

  describe('fetchMeasurements', () => {

    initTranslations({
      code: 'en',
      translation: {
        test: 'no translations will default to key',
      },
    });

    const mockHost: string = 'https://blabla.com';
    let state: ReportContainerState;
    let loggedOut: string;
    let defaultParameters: MeasurementOptions;
    const updateState = (updatedState: ReportContainerState) => state = {...updatedState};
    const logout = (error?: Unauthorized) => error ? loggedOut = error.message : 'logged out';

    const mockMeter = (medium: Medium, city = undefined, address = undefined): SelectionTreeMeter => {
      const id = idGenerator.uuid().toString();
      return ({
        address: address ? address! : idGenerator.uuid().toString(),
        city: city ? city! : idGenerator.uuid().toString(),
        id,
        medium,
        name: id,
      });
    };

    const mockCity = (medium: Medium | Medium[], addresses: string[] = []): SelectionTreeCity => {
      const id = `sweden,${idGenerator.uuid().toString()}`;
      return ({
        addresses,
        city: id,
        id,
        medium: Array.isArray(medium) ? medium : [medium],
        name: id,
      });
    };

    beforeEach(() => {
      state = initialState;
      loggedOut = 'not logged out';
      defaultParameters = {
        selectionTreeEntities: {
          addresses: {},
          cities: {},
          meters: {},
        },
        selectedIndicators: [],
        quantities: [],
        selectedListItems: [],
        timePeriod: Period.currentMonth,
        customDateRange: Maybe.nothing(),
        updateState,
        logout,
      };
    });

    it('sets default state if no quantities are provided', async () => {
      updateState({...initialState, isFetching: true});
      const fetching: ReportContainerState = {...initialState};
      expect(state).not.toEqual(fetching);

      await fetchMeasurements(defaultParameters);
      const expected: ReportContainerState = {...initialState};
      expect(state).toEqual(expected);
    });

    it('never requests addresses', async () => {
      const mockRestClient = new MockAdapter(axios);
      authenticate('test');

      const requestedUrls: string[] = [];
      mockRestClient.onGet().reply((config) => {
        requestedUrls.push(config.url!);
        return [200, 'some data'];
      });

      await fetchMeasurements({
        ...defaultParameters,
        selectedIndicators: [Medium.districtHeating],
        quantities: [Quantity.power],
        selectedListItems: ['sweden,höganäs,hasselgatan 4'],
      });

      expect(requestedUrls).toHaveLength(0);
    });

    describe('only requests quantities for meters that has them', () => {

      it('does not send requests for meters with unknown media', async () => {
        const mockRestClient = new MockAdapter(axios);
        authenticate('test');

        const requestedUrls: string[] = [];
        mockRestClient.onGet().reply((config) => {
          requestedUrls.push(config.url!);
          return [200, 'some data'];
        });

        const unknownMeter = mockMeter(Medium.unknown);

        await fetchMeasurements({
          ...defaultParameters,
          selectionTreeEntities: {
            ...defaultParameters.selectionTreeEntities,
            meters: {
              [unknownMeter.id]: unknownMeter,
            },
          },
          selectedIndicators: [Medium.districtHeating],
          quantities: [Quantity.energy],
          selectedListItems: [unknownMeter.id],
        });

        expect(requestedUrls).toHaveLength(0);
      });

      it('sends separate requests for meters with different quantities', async () => {
        const mockRestClient = new MockAdapter(axios);
        authenticate('test');

        const requestedUrls: string[] = [];
        mockRestClient.onGet().reply((config) => {
          requestedUrls.push(config.url!);
          return [200, 'some data'];
        });

        const roomSensorMeter = mockMeter(Medium.roomSensor);
        const gasMeter = mockMeter(Medium.gas);

        await fetchMeasurements({
          ...defaultParameters,
          selectionTreeEntities: {
            ...defaultParameters.selectionTreeEntities,
            meters: {
              [roomSensorMeter.id]: roomSensorMeter,
              [gasMeter.id]: gasMeter,
            },
          },
          selectedIndicators: [Medium.roomSensor, Medium.gas],
          quantities: [Quantity.externalTemperature, Quantity.volume],
          selectedListItems: [roomSensorMeter.id, gasMeter.id],
        });

        expect(requestedUrls).toHaveLength(2);
        const [externalTemperature, volume] = requestedUrls.map(
          (url: string) => new URL(`${mockHost}${url}`),
        );

        expect(externalTemperature.pathname).toEqual('/measurements');
        expect(externalTemperature.searchParams.get('quantities')).toEqual(Quantity.externalTemperature);
        expect(externalTemperature.searchParams.get('meters')).toEqual(roomSensorMeter.id);
        expect(externalTemperature.searchParams.get('before')).toBeTruthy();
        expect(externalTemperature.searchParams.get('after')).toBeTruthy();

        expect(volume.pathname).toEqual('/measurements');
        expect(volume.searchParams.get('quantities')).toEqual(Quantity.volume);
        expect(volume.searchParams.get('meters')).toEqual(gasMeter.id);
        expect(volume.searchParams.get('before')).toBeTruthy();
        expect(volume.searchParams.get('after')).toBeTruthy();
      });

      it('requests quantities for cities', async () => {
        const mockRestClient = new MockAdapter(axios);
        authenticate('test');

        const requestedUrls: string[] = [];
        mockRestClient.onGet().reply((config) => {
          requestedUrls.push(config.url!);
          return [200, 'some data'];
        });

        const roomSensorMeter = mockCity(Medium.roomSensor);
        const gasMeter = mockCity(Medium.gas);

        await fetchMeasurements({
          ...defaultParameters,
          selectionTreeEntities: {
            ...defaultParameters.selectionTreeEntities,
            cities: {
              [roomSensorMeter.id]: roomSensorMeter,
              [gasMeter.id]: gasMeter,
            },
          },
          selectedIndicators: [Medium.roomSensor, Medium.gas],
          quantities: [Quantity.externalTemperature, Quantity.volume],
          selectedListItems: [roomSensorMeter.id, gasMeter.id],
        });

        expect(requestedUrls).toHaveLength(2);

        const [externalTemperature, volume] = requestedUrls.map(
          (url: string) => new URL(`${mockHost}${url}`),
        );

        expect(externalTemperature.pathname).toEqual('/measurements/cities');
        expect(externalTemperature.searchParams.get('quantities')).toEqual(Quantity.externalTemperature);
        expect(externalTemperature.searchParams.get('city')).toEqual(roomSensorMeter.id);
        expect(externalTemperature.searchParams.get('before')).toBeTruthy();
        expect(externalTemperature.searchParams.get('after')).toBeTruthy();

        expect(volume.pathname).toEqual('/measurements/cities');
        expect(volume.searchParams.get('quantities')).toEqual(Quantity.volume);
        expect(volume.searchParams.get('city')).toEqual(gasMeter.id);
        expect(volume.searchParams.get('before')).toBeTruthy();
        expect(volume.searchParams.get('after')).toBeTruthy();
      });

    });

    describe('cities', () => {

      it('requests cities when no meters are selected', async () => {
        const mockRestClient = new MockAdapter(axios);
        authenticate('test');

        const requestedUrls: string[] = [];
        mockRestClient.onGet().reply((config) => {
          requestedUrls.push(config.url!);
          return [200, 'some data'];
        });

        const firstDistrictHeating = mockCity(Medium.districtHeating);
        const secondDistrictHeating = mockCity(Medium.districtHeating);

        await fetchMeasurements({
          ...defaultParameters,
          selectedIndicators: [Medium.districtHeating],
          quantities: [Quantity.power],
          selectedListItems: [firstDistrictHeating.id, secondDistrictHeating.id],
          selectionTreeEntities: {
            ...defaultParameters.selectionTreeEntities,
            cities: {
              [firstDistrictHeating.id]: firstDistrictHeating,
              [secondDistrictHeating.id]: secondDistrictHeating,
            },
          },
        });

        expect(requestedUrls).toHaveLength(1);

        const [url] = requestedUrls.map((url: string) => new URL(`${mockHost}${url}`));

        expect(url.pathname).toEqual('/measurements/cities');
        expect(url.searchParams.get('quantities')).toEqual(Quantity.power);
        expect(url.searchParams.getAll('city')).toEqual([
          firstDistrictHeating.id, secondDistrictHeating.id,
        ]);
        expect(url.searchParams.get('before')).toBeTruthy();
        expect(url.searchParams.get('after')).toBeTruthy();
      });

      it('requests cities when meters are selected too', async () => {
        const mockRestClient = new MockAdapter(axios);
        authenticate('test');

        const requestedUrls: string[] = [];
        mockRestClient.onGet().reply((config) => {
          requestedUrls.push(config.url!);
          return [200, 'some data'];
        });

        const mockedMeter = mockMeter(Medium.districtHeating);
        const mockedCity = mockCity(Medium.districtHeating);

        await fetchMeasurements({
          ...defaultParameters,
          selectedIndicators: [Medium.districtHeating],
          quantities: [Quantity.power],
          selectedListItems: [mockedMeter.id, mockedCity.id],
          selectionTreeEntities: {
            ...defaultParameters.selectionTreeEntities,
            cities: {
              [mockedCity.id]: mockedCity,
            },
            meters: {
              [mockedMeter.id]: mockedMeter,
            },
          },
        });

        expect(requestedUrls).toHaveLength(2);

        const [city, meter] = requestedUrls.map((url: string) => new URL(`${mockHost}${url}`));

        expect(meter.pathname).toEqual('/measurements');
        expect(meter.searchParams.get('quantities')).toEqual(Quantity.power);
        expect(meter.searchParams.get('meters')).toEqual(mockedMeter.id);
        expect(meter.searchParams.get('before')).toBeTruthy();
        expect(meter.searchParams.get('after')).toBeTruthy();

        expect(city.pathname).toEqual('/measurements/cities');
        expect(city.searchParams.get('quantities')).toEqual(Quantity.power);
        expect(city.searchParams.get('city')).toEqual(mockedCity.id);
        expect(city.searchParams.get('before')).toBeTruthy();
        expect(city.searchParams.get('after')).toBeTruthy();
      });

    });

    it('returns empty data if no meter ids are provided', async () => {
      updateState({...initialState, isFetching: true});
      const fetching: ReportContainerState = {...initialState};
      expect(state).not.toEqual(fetching);

      await fetchMeasurements({
        ...defaultParameters,
        selectedIndicators: [Medium.districtHeating],
        quantities: [Quantity.power],
        selectedListItems: [],
      });
      const expected: ReportContainerState = {...initialState};
      expect(state).toEqual(expected);
    });

    it(
      'includes average when asking for measurements for multiple meters with the same quantity',
      async () => {
        const mockRestClient = new MockAdapter(axios);
        authenticate('test');

        const requestedUrls: string[] = [];
        mockRestClient.onGet().reply((config) => {
          requestedUrls.push(config.url!);
          return [200, 'some data'];
        });

        const firstRoomSensor = mockMeter(Medium.roomSensor);
        const secondRoomSensor = mockMeter(Medium.roomSensor);

        await fetchMeasurements({
          ...defaultParameters,
          selectedIndicators: [Medium.roomSensor],
          quantities: [Quantity.externalTemperature],
          selectedListItems: [firstRoomSensor.id, secondRoomSensor.id],
          selectionTreeEntities: {
            ...defaultParameters.selectionTreeEntities,
            meters: {
              [firstRoomSensor.id]: firstRoomSensor,
              [secondRoomSensor.id]: secondRoomSensor,
            },
          },
        });
        expect(requestedUrls).toHaveLength(2);

        const [averageUrl] = requestedUrls
          .filter((url: string) => url.match('average'))
          .map((url: string) => new URL(`${mockHost}${url}`));

        expect(averageUrl.pathname).toEqual('/measurements/average');
        expect(averageUrl.searchParams.get('quantities')).toEqual(Quantity.externalTemperature);
        expect(averageUrl.searchParams.get('meters')).toEqual(
          `${firstRoomSensor.id},${secondRoomSensor.id}`,
        );
        expect(averageUrl.searchParams.get('before')).toBeTruthy();
        expect(averageUrl.searchParams.get('after')).toBeTruthy();
      },
    );

    it('provides a result suitable for parsing by mapApiResponseToGraphData', async () => {
      const mockRestClient = new MockAdapter(axios);
      authenticate('test');

      const requestedUrls: string[] = [];

      mockRestClient.onGet().reply(async (config) => {
        requestedUrls.push(config.url!);

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

        if (config.url!.match(/^\/measurements\/average/)) {
          return [200, average];
        } else {
          return [200, measurement];
        }
      });

      const firstDistrictHeating = mockMeter(Medium.districtHeating);
      const secondDistrictHeating = mockMeter(Medium.districtHeating);

      await fetchMeasurements({
        ...defaultParameters,
        selectedIndicators: [Medium.districtHeating],
        quantities: [Quantity.power],
        selectedListItems: [firstDistrictHeating.id, secondDistrictHeating.id],
        selectionTreeEntities: {
          ...defaultParameters.selectionTreeEntities,
          meters: {
            [firstDistrictHeating.id]: firstDistrictHeating,
            [secondDistrictHeating.id]: secondDistrictHeating,
          },
        },
      });

      expect(requestedUrls).toHaveLength(2);

      const {data, lines, axes: {left}}: GraphContents = mapApiResponseToGraphData(state.measurementResponse);

      expect(left).toEqual('mW');
      expect(data).toHaveLength(2);
      expect(lines).toHaveLength(3);
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

        if (config.url!.match(/^\/measurements\/average/)) {
          return [200, average];
        } else {
          return [200, measurement];
        }
      });

      const firstDistrictHeating = mockMeter(Medium.districtHeating);
      const secondDistrictHeating = mockMeter(Medium.districtHeating);

      await fetchMeasurements({
        ...defaultParameters,
        selectedIndicators: [Medium.districtHeating],
        quantities: [Quantity.power],
        selectedListItems: [firstDistrictHeating.id, secondDistrictHeating.id],
        selectionTreeEntities: {
          ...defaultParameters.selectionTreeEntities,
          meters: {
            [firstDistrictHeating.id]: firstDistrictHeating,
            [secondDistrictHeating.id]: secondDistrictHeating,
          },
        },
      });

      expect(state.measurementResponse.average).toHaveLength(1);

      expect(state.measurementResponse.average[0].values).toHaveLength(1);
      expect(state.measurementResponse.average[0].values[0].value).toBe(0.55);
    });

    it('keeps average readouts with a value of 0', async () => {
      const mockRestClient = new MockAdapter(axios);
      authenticate('test');

      const first = mockMeter(Medium.districtHeating);
      const second = mockMeter(Medium.districtHeating);

      mockRestClient.onGet().reply(async (config) => {

        const measurement: MeasurementApiResponse = [
          {
            id: first.id.toString(),
            city: first.city,
            address: first.address,
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
            id: second.id.toString(),
            city: second.city,
            address: second.address,
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

        if (config.url!.match(/^\/measurements\/average/)) {
          return [200, average];
        } else {
          return [200, measurement];
        }
      });

      await fetchMeasurements({
        ...defaultParameters,
        selectionTreeEntities: {
          ...defaultParameters.selectionTreeEntities,
          meters: {
            [first.id]: first,
            [second.id]: second,
          },
        },
        selectedIndicators: [Medium.districtHeating],
        quantities: [Quantity.power],
        selectedListItems: [first.id, second.id],
      });

      expect(state.measurementResponse.average[0].values).toHaveLength(2);
      expect(state.measurementResponse.average[0].values[0].value).toBe(0);
      expect(state.measurementResponse.average[0].values[1].value).toBe(0.55);
    });
  });
});

import axios from 'axios';
import {default as MockAdapter} from 'axios-mock-adapter';
import {Period} from '../../../../../components/dates/dateModels';
import {idGenerator} from '../../../../../helpers/idGenerator';
import {initTranslations} from '../../../../../i18n/__tests__/i18nMock';
import {authenticate} from '../../../../../services/restClient';
import {toIdNamed, uuid} from '../../../../../types/Types';
import {Unauthorized} from '../../../../../usecases/auth/authModels';
import {ReportContainerState} from '../../../../../usecases/report/containers/ReportContainer';
import {GraphContents} from '../../../../../usecases/report/reportModels';
import {SelectionTreeCity, SelectionTreeMeter} from '../../../../selection-tree/selectionTreeModels';
import {mapApiResponseToGraphData} from '../helpers/apiResponseToGraphContents';
import {fetchMeasurements, MeasurementOptions} from '../measurementActions';
import {initialState, MeasurementApiResponse, Medium, Quantity} from '../measurementModels';

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
    let defaultParameters: MeasurementOptions;
    const updateState = (updatedState: ReportContainerState) => state = {...updatedState};
    const logout = (error?: Unauthorized) => 'logged out or error';

    const mockMeter = (medium: Medium): SelectionTreeMeter => {
      const id = idGenerator.uuid().toString();
      return ({
        address: idGenerator.uuid().toString(),
        city: idGenerator.uuid().toString(),
        id,
        medium,
        name: id,
      });
    };

    const mockCity = (medium: Medium | Medium[], id: uuid | undefined = undefined): SelectionTreeCity => {
      id = id ? id.toString() : `sweden,${idGenerator.uuid().toString()}`;
      return ({
        addresses: [],
        city: id,
        id,
        medium: Array.isArray(medium) ? medium : [medium],
        name: id,
      });
    };

    beforeEach(() => {
      state = initialState;
      defaultParameters = {
        selectionTreeEntities: {
          addresses: {},
          cities: {},
          meters: {},
        },
        selectedIndicators: [],
        quantities: [],
        selectedListItems: [],
        updateState,
        logout,
        selectionParameters: {
          dateRange: {
            period: Period.currentMonth,
          }
        },
      };
    });

    describe('do not fetch', () => {

      it('sets default state if no quantities are provided', async () => {
        updateState({...initialState, isFetching: true});
        const fetching: ReportContainerState = {...initialState};
        expect(state).not.toEqual(fetching);

        await fetchMeasurements(defaultParameters);
        const expected: ReportContainerState = {...initialState};
        expect(state).toEqual(expected);
      });

      it('never requests addresses', async () => {
        const requestedUrls: string[] = onFetchAsync(
          mockMeter(Medium.districtHeating),
          mockMeter(Medium.districtHeating),
        );

        await fetchMeasurements({
          ...defaultParameters,
          selectedIndicators: [Medium.districtHeating],
          quantities: [Quantity.power],
          selectedListItems: ['sweden,höganäs,hasselgatan 4'],
        });

        expect(requestedUrls).toHaveLength(0);
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
    });

    describe('only fetch quantities for meters that have them', () => {

      it('does not send requests for meters with unknown media', async () => {
        const unknownMeter = mockMeter(Medium.unknown);
        const meter = mockMeter(Medium.unknown);

        const requestedUrls = onFetchAsync(unknownMeter, meter);

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
        const roomSensorMeter = mockMeter(Medium.roomSensor);
        const gasMeter = mockMeter(Medium.gas);
        const requestedUrls: string[] = onFetchAsync(roomSensorMeter, gasMeter);

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
        expect(externalTemperature.searchParams.get('quantity')).toEqual(Quantity.externalTemperature);
        expect(externalTemperature.searchParams.get('logicalMeterId')).toEqual(roomSensorMeter.id);
        expect(externalTemperature.searchParams.get('before')).toBeTruthy();
        expect(externalTemperature.searchParams.get('after')).toBeTruthy();

        expect(volume.pathname).toEqual('/measurements');
        expect(volume.searchParams.get('quantity')).toEqual(Quantity.volume);
        expect(volume.searchParams.get('logicalMeterId')).toEqual(gasMeter.id);
        expect(volume.searchParams.get('before')).toBeTruthy();
        expect(volume.searchParams.get('after')).toBeTruthy();
      });

      it('requests quantities for cities, and sets correct label', async () => {
        const mockRestClient = new MockAdapter(axios);
        authenticate('test');

        const requestedUrls: string[] = [];
        mockRestClient.onGet().reply((config) => {
          requestedUrls.push(config.url!);
          return [200, 'some data'];
        });

        const roomSensorMeter = mockCity(Medium.roomSensor, 'sweden,Borgholm');
        const gasMeter = mockCity(Medium.gas, 'sweden,Byxelkrok');

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

        expect(externalTemperature.pathname).toEqual('/measurements/average');
        expect(externalTemperature.searchParams.get('quantity')).toEqual(Quantity.externalTemperature);
        expect(externalTemperature.searchParams.get('city')).toEqual(roomSensorMeter.id);
        expect(externalTemperature.searchParams.get('before')).toBeTruthy();
        expect(externalTemperature.searchParams.get('after')).toBeTruthy();
        expect(externalTemperature.searchParams.get('label')).toEqual('Borgholm');

        expect(volume.pathname).toEqual('/measurements/average');
        expect(volume.searchParams.get('quantity')).toEqual(Quantity.volume);
        expect(volume.searchParams.get('city')).toEqual(gasMeter.id);
        expect(volume.searchParams.get('before')).toBeTruthy();
        expect(volume.searchParams.get('after')).toBeTruthy();
        expect(volume.searchParams.get('label')).toEqual('Byxelkrok');
      });

    });

    describe('fetch cities', () => {

      it('requests per city and average', async () => {
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

        expect(requestedUrls).toHaveLength(2);

        const [firstUrl, secondUrl] = requestedUrls.map((url: string) => new URL(`${mockHost}${url}`));

        expect(firstUrl.pathname).toEqual('/measurements/average');
        expect(firstUrl.searchParams.get('quantity')).toEqual(Quantity.power);
        expect(firstUrl.searchParams.get('city')).toEqual(firstDistrictHeating.id);
        expect(firstUrl.searchParams.get('before')).toBeTruthy();
        expect(firstUrl.searchParams.get('after')).toBeTruthy();

        expect(secondUrl.pathname).toEqual('/measurements/average');
        expect(secondUrl.searchParams.get('quantity')).toEqual(Quantity.power);
        expect(secondUrl.searchParams.get('city')).toEqual(secondDistrictHeating.id);
        expect(secondUrl.searchParams.get('before')).toBeTruthy();
        expect(secondUrl.searchParams.get('after')).toBeTruthy();
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
        expect(meter.searchParams.get('quantity')).toEqual(Quantity.power);
        expect(meter.searchParams.get('logicalMeterId')).toEqual(mockedMeter.id);
        expect(meter.searchParams.get('before')).toBeTruthy();
        expect(meter.searchParams.get('after')).toBeTruthy();

        expect(city.pathname).toEqual('/measurements/average');
        expect(city.searchParams.get('quantity')).toEqual(Quantity.power);
        expect(city.searchParams.get('city')).toEqual(mockedCity.id);
        expect(city.searchParams.get('before')).toBeTruthy();
        expect(city.searchParams.get('after')).toBeTruthy();
      });

      it('current selection affects the *average* request URL', async () => {
        const mockRestClient = new MockAdapter(axios);
        authenticate('test');

        const requestedUrls: string[] = [];
        mockRestClient.onGet().reply((config) => {
          requestedUrls.push(config.url!);
          return [200, 'some data'];
        });

        const mockedCity = mockCity(Medium.districtHeating, 'sweden,göteborg');

        await fetchMeasurements({
          ...defaultParameters,
          selectedIndicators: [Medium.districtHeating],
          quantities: [Quantity.power],
          selectedListItems: [mockedCity.id],
          selectionTreeEntities: {
            ...defaultParameters.selectionTreeEntities,
            cities: {
              [mockedCity.id]: mockedCity,
            },
          },
          selectionParameters: {
            ...defaultParameters.selectionParameters,
            media: [{...toIdNamed('Gas')}],
          }
        });

        expect(requestedUrls).toHaveLength(1);

        const [city] = requestedUrls.map((url: string) => new URL(`${mockHost}${url}`));

        expect(city.pathname).toEqual('/measurements/average');
        expect(city.searchParams.get('quantity')).toEqual(Quantity.power);
        expect(city.searchParams.get('city')).toEqual('sweden,göteborg');
        expect(city.searchParams.get('label')).toEqual('Göteborg');
        expect(city.searchParams.get('before')).toBeTruthy();
        expect(city.searchParams.get('after')).toBeTruthy();
        expect(city.searchParams.get('medium')).toEqual('Gas');
      });

    });

    describe('fetch average ', () => {

      it('includes average when asking for measurements for multiple meters with the same quantity', async () => {
        const firstRoomSensor = mockMeter(Medium.roomSensor);
        const secondRoomSensor = mockMeter(Medium.roomSensor);
        const requestedUrls: string[] = onFetchAsync(firstRoomSensor, secondRoomSensor);

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
        expect(averageUrl.searchParams.get('quantity')).toEqual(Quantity.externalTemperature);
        expect(averageUrl.searchParams.getAll('logicalMeterId')).toEqual(
          [firstRoomSensor.id, secondRoomSensor.id]
        );
        expect(averageUrl.searchParams.get('before')).toBeTruthy();
        expect(averageUrl.searchParams.get('after')).toBeTruthy();
      });

      it('provides a result suitable for parsing by mapApiResponseToGraphData', async () => {
        const firstDistrictHeating = mockMeter(Medium.districtHeating);
        const secondDistrictHeating = mockMeter(Medium.districtHeating);
        const requestedUrls: string[] = onFetchAsync(firstDistrictHeating, secondDistrictHeating);

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
        const firstDistrictHeating = mockMeter(Medium.districtHeating);
        const secondDistrictHeating = mockMeter(Medium.districtHeating);

        onFetchAsync(firstDistrictHeating, secondDistrictHeating);

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
        expect(state.measurementResponse.average[0].values).toHaveLength(2);
        expect(state.measurementResponse.average[0].values[1].value).toBe(0.55);
      });

      it('keeps average readouts with a value of 0', async () => {
        const first = mockMeter(Medium.districtHeating);
        const second = mockMeter(Medium.districtHeating);

        onFetchAsync(first, second);

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

      it('does not include meters that are not within the meter entities', async () => {
        const first = mockMeter(Medium.districtHeating);
        const second = mockMeter(Medium.districtHeating);

        onFetchAsync(first, second);

        await fetchMeasurements({
          ...defaultParameters,
          selectedIndicators: [Medium.districtHeating],
          quantities: [Quantity.power],
          selectedListItems: [first.id, second.id],
        });

        expect(state.measurementResponse).toEqual({average: [], measurements: [], cities: []});
      });

      it('does not include cities that are not within the cities entities', async () => {
        const first = mockMeter(Medium.districtHeating);
        const second = mockMeter(Medium.districtHeating);

        onFetchAsync(first, second);

        await fetchMeasurements({
          ...defaultParameters,
          selectedIndicators: [Medium.districtHeating],
          quantities: [Quantity.power],
          selectedListItems: ['sto,bkk'],
        });

        expect(state.measurementResponse).toEqual({average: [], measurements: [], cities: []});
      });
    });

    const onFetchAsync = (meter1: SelectionTreeMeter, meter2: SelectionTreeMeter): string[] => {
      const requestedUrls: string[] = [];
      const mockRestClient = new MockAdapter(axios);

      authenticate('test');

      mockRestClient.onGet().reply(async (config) => {
        requestedUrls.push(config.url!);

        const measurement: MeasurementApiResponse = [
          {
            id: meter1.id.toString(),
            city: meter1.city,
            address: meter1.address,
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
            id: meter2.id.toString(),
            city: meter2.city,
            address: meter2.address,
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
                when: 1516521583309,
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

      return requestedUrls;
    };
  });
});

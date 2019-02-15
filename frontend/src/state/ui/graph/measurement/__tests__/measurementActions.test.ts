import axios from 'axios';
import {default as MockAdapter} from 'axios-mock-adapter';
import {routerActions} from 'react-router-redux';
import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {routes} from '../../../../../app/routes';
import {Period, TemporalResolution} from '../../../../../components/dates/dateModels';
import {InvalidToken} from '../../../../../exceptions/InvalidToken';
import {idGenerator} from '../../../../../helpers/idGenerator';
import {Maybe} from '../../../../../helpers/Maybe';
import {initTranslations} from '../../../../../i18n/__tests__/i18nMock';
import {authenticate} from '../../../../../services/restClient';
import {logoutUser} from '../../../../../usecases/auth/authActions';
import {AuthState, Unauthorized} from '../../../../../usecases/auth/authModels';
import {LegendItem} from '../../../../../usecases/report/reportModels';
import {noInternetConnection, requestTimeout} from '../../../../api/apiActions';
import {Role, User} from '../../../../domain-models/user/userModels';
import {SelectionTreeMeter} from '../../../../selection-tree/selectionTreeModels';
import {
  exportToExcel,
  exportToExcelAction,
  fetchMeasurements,
  MEASUREMENT_REQUEST,
  MEASUREMENT_SUCCESS,
  measurementFailure,
  MeasurementParameters,
  measurementRequest,
  measurementSuccess
} from '../measurementActions';
import {MeasurementApiResponse, MeasurementResponse, MeasurementState, Medium, Quantity} from '../measurementModels';
import {initialState} from '../measurementReducer';

describe('measurementActions', () => {

  const configureMockStore = configureStore([thunk]);

  const storeWith = (measurement: MeasurementState, auth?: AuthState) => configureMockStore({measurement, auth});

  describe('fetchMeasurements', () => {

    initTranslations({
      code: 'en',
      translation: {
        test: 'no translations will default to key',
      },
    });

    const mockHost: string = 'https://blabla.com';
    let defaultParameters: MeasurementParameters;

    const mockMeter = (medium: Medium): SelectionTreeMeter => {
      const id = idGenerator.uuid().toString();
      return ({
        address: 'kingstreet 1',
        city: 'kungsbacka',
        id,
        medium,
        name: id,
      });
    };

    const toLegendItem = ({id, name: label, medium}: SelectionTreeMeter): LegendItem => ({id, label, medium});

    beforeEach(() => {
      defaultParameters = {
        items: [],
        quantities: [],
        resolution: TemporalResolution.day,
        selectionParameters: {
          dateRange: {
            period: Period.currentMonth,
          }
        },
        shouldMakeAverageRequest: true,
      };
    });

    describe('do not fetch', () => {

      it('should not dispatch any actions when nothing is pre-selected', async () => {
        const store = storeWith(initialState);

        await store.dispatch(fetchMeasurements(defaultParameters));

        expect(store.getActions()).toEqual([]);
      });

      it('never requests addresses', async () => {
        const store = storeWith(initialState);

        const requestedUrls: string[] = onFetchAsync(
          mockMeter(Medium.districtHeating),
          mockMeter(Medium.districtHeating),
        );

        await store.dispatch(fetchMeasurements({
          ...defaultParameters,
          quantities: [Quantity.power],
        }));

        expect(requestedUrls).toHaveLength(0);
        expect(store.getActions()).toEqual([]);
      });

      it('returns empty data if no meter ids are provided', async () => {
        const store = storeWith(initialState);

        await store.dispatch(fetchMeasurements({
          ...defaultParameters,
          quantities: [Quantity.power],
        }));

        expect(store.getActions()).toEqual([]);
      });

      it('should not fetch when already fetching', async () => {
        const store = storeWith({...initialState, isFetching: true});

        await store.dispatch(fetchMeasurements(defaultParameters));

        expect(store.getActions()).toEqual([]);
      });

      it('should not fetch when is has successfully fetched measurements', async () => {
        const store = storeWith({...initialState, isFetching: false, isSuccessfullyFetched: true});

        await store.dispatch(fetchMeasurements(defaultParameters));

        expect(store.getActions()).toEqual([]);
      });

      it('should not fetch when there is an error that is not cleared yet', async () => {
        const store = storeWith({...initialState, error: Maybe.just({message: 'error'})});

        await store.dispatch(fetchMeasurements(defaultParameters));

        expect(store.getActions()).toEqual([]);
      });
    });

    describe('only fetch quantities for meters that have them', () => {

      it('does not send requests for meters with unknown media', async () => {
        const unknownMeter = mockMeter(Medium.unknown);
        const meter = mockMeter(Medium.unknown);

        const store = storeWith(initialState);

        const requestedUrls = onFetchAsync(unknownMeter, meter);

        await store.dispatch(fetchMeasurements({
          ...defaultParameters,
          items: [],
          quantities: [Quantity.energy],
        }));

        expect(requestedUrls).toHaveLength(0);
        expect(store.getActions()).toEqual([]);
      });

      const makePayload = (meter1, meter2): MeasurementResponse => ({
        measurements: [
          {
            id: meter1.id as string,
            city: 'kungsbacka',
            address: 'kingstreet 1',
            quantity: Quantity.power,
            medium: 'Electricity',
            values: [{when: 1516521585107, value: 0.4353763591158477}],
            label: '1',
            unit: 'mW'
          },
          {
            id: meter2.id as string,
            city: 'kungsbacka',
            address: 'kingstreet 1',
            quantity: Quantity.power,
            medium: 'Electricity',
            values: [{when: 1516521585107, value: 0.4353763591158477}],
            label: '2',
            unit: 'mW'
          },
          {
            id: meter1.id as string,
            city: 'kungsbacka',
            address: 'kingstreet 1',
            quantity: Quantity.power,
            medium: 'Electricity',
            values: [{when: 1516521585107, value: 0.4353763591158477}],
            label: '1',
            unit: 'mW'
          },
          {
            id: meter2.id as string,
            city: 'kungsbacka',
            address: 'kingstreet 1',
            quantity: Quantity.power,
            medium: 'Electricity',
            values: [{when: 1516521585107, value: 0.4353763591158477}],
            label: '2',
            unit: 'mW'
          }
        ],
        average: [],
      });

      it('sends separate requests for meters with different quantities', async () => {
        const roomSensorMeter = mockMeter(Medium.roomSensor);
        const gasMeter = mockMeter(Medium.gas);
        const requestedUrls: string[] = onFetchAsync(roomSensorMeter, gasMeter);
        const store = storeWith(initialState);

        await store.dispatch(fetchMeasurements({
          ...defaultParameters,
          quantities: [Quantity.externalTemperature, Quantity.volume],
          items: [toLegendItem(roomSensorMeter), toLegendItem(gasMeter)]
        }));

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

        expect(store.getActions()).toEqual([
          measurementRequest(),
          measurementSuccess(makePayload(roomSensorMeter, gasMeter))
        ]);
      });

    });

    describe('fetch average ', () => {

      it('includes average when asking for measurements for multiple meters with the same quantity', async () => {
        const store = storeWith(initialState);
        const firstRoomSensor = mockMeter(Medium.roomSensor);
        const secondRoomSensor = mockMeter(Medium.roomSensor);
        const requestedUrls: string[] = onFetchAsync(firstRoomSensor, secondRoomSensor);

        await store.dispatch(fetchMeasurements({
          ...defaultParameters,
          quantities: [Quantity.externalTemperature],
          items: [toLegendItem(firstRoomSensor), toLegendItem(secondRoomSensor)],
        }));

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
        expect(store.getActions().map((action) => (action.type))).toEqual([MEASUREMENT_REQUEST, MEASUREMENT_SUCCESS]);
      });

      it('makes one measurements and one average requests', async () => {
        const store = storeWith(initialState);
        const firstDistrictHeating = mockMeter(Medium.districtHeating);
        const secondDistrictHeating = mockMeter(Medium.districtHeating);
        const requestedUrls: string[] = onFetchAsync(firstDistrictHeating, secondDistrictHeating);

        await store.dispatch(fetchMeasurements({
          ...defaultParameters,
          quantities: [Quantity.power],
          items: [toLegendItem(firstDistrictHeating), toLegendItem(secondDistrictHeating)],
        }));

        expect(requestedUrls).toHaveLength(2);
        expect(store.getActions().map((action) => (action.type))).toEqual([MEASUREMENT_REQUEST, MEASUREMENT_SUCCESS]);
      });

      it('filters out average readouts without values', async () => {
        const store = storeWith(initialState);
        const firstDistrictHeating = mockMeter(Medium.districtHeating);
        const secondDistrictHeating = mockMeter(Medium.districtHeating);

        onFetchAsync(firstDistrictHeating, secondDistrictHeating);

        await store.dispatch(fetchMeasurements({
          ...defaultParameters,
          quantities: [Quantity.power],
          items: [toLegendItem(firstDistrictHeating), toLegendItem(secondDistrictHeating)],
        }));

        expect(store.getActions()).toEqual([
          measurementRequest(),
          {
            type: MEASUREMENT_SUCCESS,
            payload:
              {
                measurements:
                  [
                    {
                      id: firstDistrictHeating.id as string,
                      city: 'kungsbacka',
                      address: 'kingstreet 1',
                      quantity: 'Power',
                      medium: 'Electricity',
                      values: [{when: 1516521585107, value: 0.4353763591158477}],
                      label: '1',
                      unit: 'mW'
                    },
                    {
                      id: secondDistrictHeating.id as string,
                      city: 'kungsbacka',
                      address: 'kingstreet 1',
                      quantity: 'Power',
                      medium: 'Electricity',
                      values: [{when: 1516521585107, value: 0.4353763591158477}],
                      label: '2',
                      unit: 'mW'
                    }
                  ],
                average:
                  [
                    {
                      id: 'Varberg',
                      city: 'Varberg',
                      address: '',
                      quantity: 'Power',
                      unit: 'mW',
                      label: 'average',
                      medium: 'Electricity',
                      values:
                        [
                          {when: 1516521585107, value: 0},
                          {when: 1516521585109, value: 0.55}
                        ]
                    }
                  ],
              }
          }
        ]);
      });

      it('keeps average readouts with a value of 0', async () => {
        const store = storeWith(initialState);
        const first = mockMeter(Medium.districtHeating);
        const second = mockMeter(Medium.districtHeating);

        onFetchAsync(first, second);

        await store.dispatch(fetchMeasurements({
          ...defaultParameters,
          quantities: [Quantity.power],
          items: [toLegendItem(first), toLegendItem(second)],
        }));

        expect(store.getActions()).toEqual([
          measurementRequest(),
          {
            type: MEASUREMENT_SUCCESS,
            payload:
              {
                measurements:
                  [
                    {
                      id: first.id as string,
                      city: 'kungsbacka',
                      address: 'kingstreet 1',
                      quantity: 'Power',
                      medium: 'Electricity',
                      values: [{when: 1516521585107, value: 0.4353763591158477}],
                      label: '1',
                      unit: 'mW'
                    },
                    {
                      id: second.id as string,
                      city: 'kungsbacka',
                      address: 'kingstreet 1',
                      quantity: 'Power',
                      medium: 'Electricity',
                      values: [{when: 1516521585107, value: 0.4353763591158477}],
                      label: '2',
                      unit: 'mW'
                    }
                  ],
                average:
                  [
                    {
                      id: 'Varberg',
                      city: 'Varberg',
                      address: '',
                      quantity: 'Power',
                      unit: 'mW',
                      label: 'average',
                      medium: 'Electricity',
                      values:
                        [
                          {when: 1516521585107, value: 0},
                          {when: 1516521585109, value: 0.55}
                        ]
                    }
                  ],
              }
          }
        ]);
      });

      it('does not fetch measurements when there are not items to fetch', async () => {
        const store = storeWith(initialState);
        const first = mockMeter(Medium.districtHeating);
        const second = mockMeter(Medium.districtHeating);

        onFetchAsync(first, second);

        await store.dispatch(fetchMeasurements({
          ...defaultParameters,
          quantities: [Quantity.power],
          items: [],
        }));

        expect(store.getActions()).toEqual([]);
      });

    });

    describe('handle request errors', () => {
      let errorMockRestClient;
      let store;

      beforeEach(() => {
        store = storeWith(initialState);
        errorMockRestClient = new MockAdapter(axios);
        authenticate('test');
      });

      it('logs out user when token is invalid', async () => {
        const user: User = {
          id: 1,
          name: 'clark',
          email: 'ck@dailyplanet.net',
          language: 'sv',
          organisation: {id: 'daily planet', name: 'daily planet', slug: 'daily-planet'},
          roles: [Role.USER],
        };
        store = storeWith(initialState, {user, isAuthenticated: true});
        const error = new InvalidToken('Token missing or invalid');

        (() => errorMockRestClient.onGet().reply(async () => [401, error]))();

        await onFetchMeasurements();

        expect(store.getActions()).toEqual([
          measurementRequest(),
          logoutUser(error as Unauthorized),
          routerActions.push(`${routes.login}/${user.organisation.slug}`),
        ]);
      });

      it('handles request timeouts', async () => {
        (() => errorMockRestClient.onGet().timeout())();

        await onFetchMeasurements();

        expect(store.getActions()).toEqual([
          measurementRequest(),
          measurementFailure(Maybe.just(requestTimeout()))
        ]);
      });

      it('handles network errors', async () => {
        (() => errorMockRestClient.onGet().networkError())();

        await onFetchMeasurements();

        expect(store.getActions()).toEqual([
          measurementRequest(),
          measurementFailure(Maybe.just(noInternetConnection()))
        ]);
      });

      it('handles custom error messages', async () => {
        const response = {message: 'error'};
        (() => errorMockRestClient.onGet().reply(() => [500, response]))();

        await onFetchMeasurements();

        expect(store.getActions()).toEqual([
          measurementRequest(),
          measurementFailure(Maybe.maybe(response))
        ]);
      });

      const onFetchMeasurements = async () => {
        const meter = mockMeter(Medium.districtHeating);

        await store.dispatch(fetchMeasurements({
          ...defaultParameters,
          quantities: [Quantity.power],
          items: [toLegendItem(meter)],
        }));
      };

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

  describe('exportToExcel', () => {

    it('dispatches action if no export is ongoing', () => {
      const store = storeWith(initialState);

      store.dispatch(exportToExcel());

      expect(store.getActions()).toEqual([exportToExcelAction()]);
    });

    it('does not dispatch action if export is ongoing', () => {
      const store = storeWith({
        ...initialState,
        isExportingToExcel: true,
      });

      store.dispatch(exportToExcel());

      expect(store.getActions()).toEqual([]);
    });

  });

});

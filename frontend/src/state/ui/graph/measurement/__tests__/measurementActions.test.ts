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
import {LegendItem, SelectedReportItems} from '../../../../../usecases/report/reportModels';
import {noInternetConnection, requestTimeout} from '../../../../api/apiActions';
import {Role, User} from '../../../../domain-models/user/userModels';
import {
  exportToExcel,
  exportToExcelAction,
  fetchMeasurements,
  MEASUREMENT_REQUEST,
  MEASUREMENT_SUCCESS,
  measurementFailure,
  measurementRequest,
  measurementSuccess
} from '../measurementActions';
import {
  allQuantities,
  AverageResponsePart,
  MeasurementApiResponse, MeasurementParameters,
  MeasurementState,
  MeasurementValues,
  Medium,
  Quantity,
  toMediumText
} from '../measurementModels';
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

    const legendItemOf = (medium: Medium, label: string = 'facility-1'): LegendItem => {
      const id = idGenerator.uuid().toString();
      return ({id, medium, label, isHidden: false, quantities: [allQuantities[medium][0]]});
    };

    const makeMeasurementResponse = ({id, label}: LegendItem) => ({
      id: id.toString(),
      label,
      city: 'Varberg',
      address: '',
      quantity: Quantity.power,
      medium: toMediumText(Medium.electricity),
      values: [{when: 1516521585107, value: 0.4353763591158477}],
      unit: 'mW',
    });

    // const justValues: MeasurementValues = [
    //   {when: 1516521585107, value: 0.0},
    //   {when: 1516521585109, value: 0.55},
    // ];

    const values: MeasurementValues = [
      {when: 1516521585107, value: 0.0},
      {when: 1516521583309},
      {when: 1516521585109, value: 0.55},
    ];

    const makeMeasurementAverageResponse = (): AverageResponsePart =>
      ({
          quantity: Quantity.power,
          id: 'Varberg',
          label: 'average',
          values,
          unit: ' mW',
        }
      );

    const selectedReportItems: SelectedReportItems = {meters: []};

    beforeEach(() => {
      defaultParameters = {
        resolution: TemporalResolution.day,
        selectionParameters: {
          dateRange: {
            period: Period.currentMonth,
          }
        },
        selectedReportItems,
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
          legendItemOf(Medium.districtHeating),
          legendItemOf(Medium.districtHeating),
        );

        await store.dispatch(fetchMeasurements(defaultParameters));

        expect(requestedUrls).toHaveLength(0);
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
        const unknownMeter = legendItemOf(Medium.unknown);
        const meter = legendItemOf(Medium.unknown);

        const store = storeWith(initialState);

        const requestedUrls = onFetchAsync(unknownMeter, meter);

        await store.dispatch(fetchMeasurements({...defaultParameters}));

        expect(requestedUrls).toHaveLength(0);
        expect(store.getActions()).toEqual([]);
      });

      it('sends separate requests for meters with different quantities', async () => {
        const roomSensorMeter = legendItemOf(Medium.roomSensor);
        const gasMeter = legendItemOf(Medium.gas, 'facility-gas');
        const store = storeWith(initialState);

        const requestedUrls: string[] = onFetchAsync(roomSensorMeter, gasMeter);

        await store.dispatch(fetchMeasurements({
          ...defaultParameters,
          selectedReportItems: {
            ...selectedReportItems,
            meters: [roomSensorMeter, gasMeter]
          }
        }));

        expect(requestedUrls).toHaveLength(2);

        const [externalTemperature, volume] = requestedUrls.map(
          (url: string) => new URL(`${mockHost}${url}`),
        );

        expect(externalTemperature.pathname).toEqual('/measurements');
        expect(externalTemperature.searchParams.get('quantity')).toEqual(Quantity.volume);
        expect(externalTemperature.searchParams.get('logicalMeterId')).toEqual(gasMeter.id);
        expect(externalTemperature.searchParams.get('before')).toBeTruthy();
        expect(externalTemperature.searchParams.get('after')).toBeTruthy();

        expect(volume.pathname).toEqual('/measurements');
        expect(volume.searchParams.get('quantity')).toEqual(Quantity.externalTemperature);
        expect(volume.searchParams.get('logicalMeterId')).toEqual(roomSensorMeter.id);
        expect(volume.searchParams.get('before')).toBeTruthy();
        expect(volume.searchParams.get('after')).toBeTruthy();

        expect(store.getActions()).toEqual([
          measurementRequest(),
          measurementSuccess({
            measurements: [
              makeMeasurementResponse(roomSensorMeter),
              makeMeasurementResponse(roomSensorMeter),
              makeMeasurementResponse(gasMeter),
              makeMeasurementResponse(gasMeter),
            ],
            average: [],
          })
        ]);
      });

    });

    describe('fetch average ', () => {

      // TODO[!must!] resolve when impl. average

      // it('includes average when asking for measurements for multiple meters with the same quantity', async () => {
      //   const store = storeWith(initialState);
      //   const firstRoomSensor = legendItemOf(Medium.roomSensor);
      //   const secondRoomSensor = legendItemOf(Medium.roomSensor);
      //   const requestedUrls: string[] = onFetchAsync(firstRoomSensor, secondRoomSensor);
      //
      //   await store.dispatch(fetchMeasurements({
      //     ...defaultParameters,
      //     selectedReportItems: {
      //       ...selectedReportItems,
      //       items: [firstRoomSensor, secondRoomSensor],
      //     },
      //   }));
      //
      //   expect(requestedUrls).toHaveLength(4);
      //
      //   const [averageUrl] = requestedUrls
      //     .filter((url: string) => url.match('average'))
      //     .map((url: string) => new URL(`${mockHost}${url}`));
      //
      //   expect(averageUrl.pathname).toEqual('/measurements/average');
      //   expect(averageUrl.searchParams.get('quantity')).toEqual(Quantity.externalTemperature);
      //   expect(averageUrl.searchParams.getAll('logicalMeterId')).toEqual(
      //     [firstRoomSensor.id, secondRoomSensor.id]
      //   );
      //   expect(averageUrl.searchParams.get('before')).toBeTruthy();
      //   expect(averageUrl.searchParams.get('after')).toBeTruthy();
      //   expect(store.getActions().map((action) => (action.type))).toEqual([MEASUREMENT_REQUEST,
      // MEASUREMENT_SUCCESS]); });

      it('makes one measurements and one average requests', async () => {
        const store = storeWith(initialState);
        const firstDistrictHeating = legendItemOf(Medium.districtHeating);
        const secondDistrictHeating = legendItemOf(Medium.districtHeating);
        const requestedUrls: string[] = onFetchAsync(firstDistrictHeating, secondDistrictHeating);

        await store.dispatch(fetchMeasurements({
          ...defaultParameters,
          selectedReportItems: {
            ...selectedReportItems,
            meters: [firstDistrictHeating, secondDistrictHeating],
          }
        }));

        expect(requestedUrls).toHaveLength(1);
        expect(store.getActions().map((action) => (action.type))).toEqual([MEASUREMENT_REQUEST, MEASUREMENT_SUCCESS]);
      });

      // TODO[!must!] resolve when impl. average

      // it('filters out average readouts without values', async () => {
      //   const store = storeWith(initialState);
      //   const first = legendItemOf(Medium.districtHeating);
      //   const second = legendItemOf(Medium.districtHeating, 'facility-2');
      //
      //   onFetchAsync(first, second);
      //
      //   await store.dispatch(fetchMeasurements({
      //     ...defaultParameters,
      //     selectedReportItems: {
      //       ...selectedReportItems,
      //       items: [first, second],
      //     }
      //   }));
      //
      //   expect(store.getActions()).toEqual([
      //     measurementRequest(),
      //     measurementSuccess({
      //       measurements: [
      //         ...allQuantities[first.medium].map((_) => makeMeasurementResponse(first)),
      //         ...allQuantities[second.medium].map((_) => makeMeasurementResponse(second)),
      //       ],
      //       average: allQuantities[first.medium].map((_) => ({
      //         ...makeMeasurementAverageResponse(),
      //         values: justValues
      //       })),
      //     }),
      //   ]);
      // });

      // TODO[!must!] resolve when impl. average

      // it('keeps average readouts with a value of 0', async () => {
      //   const store = storeWith(initialState);
      //   const first = legendItemOf(Medium.districtHeating);
      //   const second = legendItemOf(Medium.districtHeating, 'facility-2');
      //
      //   onFetchAsync(first, second);
      //
      //   await store.dispatch(fetchMeasurements({
      //     ...defaultParameters,
      //     selectedReportItems: {
      //       ...selectedReportItems,
      //       items: [first, second],
      //     }
      //   }));
      //
      //   expect(store.getActions()).toEqual([
      //     measurementRequest(),
      //     measurementSuccess({
      //       measurements: [
      //         ...allQuantities[first.medium].map((_) => makeMeasurementResponse(first)),
      //         ...allQuantities[second.medium].map((_) => makeMeasurementResponse(second))
      //       ],
      //       average: allQuantities[first.medium].map((_) => ({
      //         ...makeMeasurementAverageResponse(),
      //         values: justValues
      //       })),
      //     }),
      //   ]);
      // });

      it('does not fetch measurements when there are not items to fetch', async () => {
        const store = storeWith(initialState);
        const first = legendItemOf(Medium.districtHeating);
        const second = legendItemOf(Medium.districtHeating);

        onFetchAsync(first, second);

        await store.dispatch(fetchMeasurements(defaultParameters));

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
        const meter = legendItemOf(Medium.districtHeating);

        await store.dispatch(fetchMeasurements({
          ...defaultParameters,
          selectedReportItems: {
            ...selectedReportItems,
            meters: [meter],
          }
        }));
      };

    });

    const onFetchAsync = (meter1: LegendItem, meter2: LegendItem): string[] => {
      const requestedUrls: string[] = [];
      const mockRestClient = new MockAdapter(axios);

      authenticate('test');

      mockRestClient.onGet().reply(async (config) => {
        requestedUrls.push(config.url!);

        const measurement: MeasurementApiResponse = [
          makeMeasurementResponse(meter1),
          makeMeasurementResponse(meter2),
        ];

        const average = [makeMeasurementAverageResponse()];

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

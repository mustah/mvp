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
import {EndPoints} from '../../../../../services/endPoints';
import {authenticate} from '../../../../../services/restClient';
import {toIdNamed} from '../../../../../types/Types';
import {logoutUser} from '../../../../../usecases/auth/authActions';
import {AuthState, Unauthorized} from '../../../../../usecases/auth/authModels';
import {toAggregateLegendItem} from '../../../../../usecases/report/helpers/legendHelper';
import {isAggregate, isMedium, LegendItem} from '../../../../../usecases/report/reportModels';
import {noInternetConnection, requestTimeout} from '../../../../api/apiActions';
import {NormalizedState} from '../../../../domain-models/domainModels';
import {initialDomain} from '../../../../domain-models/domainModelsReducer';
import {Role, User} from '../../../../domain-models/user/userModels';
import {ParameterName, UserSelection} from '../../../../user-selection/userSelectionModels';
import {initialState as initialUserSelectionState} from '../../../../user-selection/userSelectionReducer';
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
  allQuantitiesMap,
  MeasurementParameters,
  MeasurementResponsePart,
  MeasurementsApiResponse,
  MeasurementState,
  MeasurementValue,
  Medium,
  Quantity,
  toMediumText
} from '../measurementModels';
import {initialState} from '../measurementReducer';

describe('measurementActions', () => {

  const configureMockStore = configureStore([thunk]);

  const storeWith = (measurement: MeasurementState, auth?: AuthState) =>
    configureMockStore({measurement, auth});

  describe('fetchMeasurements', () => {

    initTranslations({
      code: 'en',
      translation: {
        test: 'no translations will default to key',
      },
    });

    const mockHost: string = 'https://blabla.com';

    const legendItemOf = (medium: Medium, label: string = 'facility-1'): LegendItem => {
      const id = idGenerator.uuid().toString();
      return ({id, type: medium, label, isHidden: false, quantities: [allQuantitiesMap[medium][0]]});
    };

    const justValues: MeasurementValue[] = [
      {when: 1516521585107, value: 0.0},
      {when: 1516521585109, value: 0.55},
    ];

    const values: MeasurementValue[] = [
      {when: 1516521585107, value: 0.0},
      {when: 1516521583309},
      {when: 1516521585109, value: 0.55},
    ];

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

    const makeMeasurementAverageResponse = ({id, label}: LegendItem): MeasurementResponsePart =>
      ({id: id.toString(), label, quantity: Quantity.flow, values, unit: ' mW'});

    const parameters: MeasurementParameters = {
      legendItems: [],
      resolution: TemporalResolution.day,
      selectionParameters: {
        dateRange: {
          period: Period.currentMonth,
        }
      },
      shouldComparePeriod: false,
    };

    describe('do not fetch', () => {

      it('should not dispatch any actions when nothing is pre-selected', async () => {
        const store = storeWith(initialState);

        await store.dispatch(fetchMeasurements(parameters));

        expect(store.getActions()).toEqual([]);
      });

      it('never requests addresses', async () => {
        const store = storeWith(initialState);

        const requestedUrls: string[] = onFetchAsync([
          legendItemOf(Medium.districtHeating),
          legendItemOf(Medium.districtHeating),
        ]);

        await store.dispatch(fetchMeasurements(parameters));

        expect(requestedUrls).toHaveLength(0);
        expect(store.getActions()).toEqual([]);
      });

      it('should not fetch when already fetching', async () => {
        const store = storeWith({...initialState, isFetching: true});

        await store.dispatch(fetchMeasurements(parameters));

        expect(store.getActions()).toEqual([]);
      });

      it('should not fetch when is has successfully fetched measurements', async () => {
        const store = storeWith({...initialState, isFetching: false, isSuccessfullyFetched: true});

        await store.dispatch(fetchMeasurements(parameters));

        expect(store.getActions()).toEqual([]);
      });

      it('should not fetch when there is an error that is not cleared yet', async () => {
        const store = storeWith({...initialState, error: Maybe.just({message: 'error'})});

        await store.dispatch(fetchMeasurements(parameters));

        expect(store.getActions()).toEqual([]);
      });
    });

    describe('only fetch quantities for meters that have them', () => {

      it('does not send requests for meters with unknown media', async () => {
        const store = storeWith(initialState);

        const requestedUrls = onFetchAsync([legendItemOf(Medium.unknown), legendItemOf(Medium.unknown)]);

        await store.dispatch(fetchMeasurements({...parameters}));

        expect(requestedUrls).toHaveLength(0);
        expect(store.getActions()).toEqual([]);
      });

      it('sends separate requests for meters with different quantities', async () => {
        const roomSensorMeter = legendItemOf(Medium.roomSensor);
        const gasMeter = legendItemOf(Medium.gas, 'facility-gas');
        const legendItems = [roomSensorMeter, gasMeter];

        const store = storeWith(initialState);

        const requestedUrls: string[] = onFetchAsync(legendItems);

        await store.dispatch(fetchMeasurements({...parameters, legendItems}));

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
            compare: [],
          })
        ]);
      });

    });

    describe('fetch average ', () => {

      const stockholm = toIdNamed('sverige,stockholm,street');
      const userSelection: UserSelection = {
        ...initialUserSelectionState.userSelection,
        id: 1,
        selectionParameters: {
          ...initialUserSelectionState.userSelection.selectionParameters,
          [ParameterName.cities]: [stockholm],
        },
      };
      const userSelections: NormalizedState<UserSelection> = {
        ...initialDomain<UserSelection>(),
        result: [userSelection.id],
        entities: {[userSelection.id]: {...userSelection}},
      };

      let store;
      beforeEach(() => {
        store = configureMockStore({measurement: initialState, domainModels: {userSelections}});
      });

      it('for single legend item having aggregate type with a user selection', async () => {
        const legendItems = [toAggregateLegendItem({id: 1, name: 'foo'})];

        const requestedUrls: string[] = onFetchAsync(legendItems);

        await store.dispatch(fetchMeasurements({...parameters, legendItems}));

        expect(requestedUrls).toHaveLength(1);

        const [url] = requestedUrls.map(url => new URL(`${mockHost}${url}`));

        expect(url.pathname).toEqual(EndPoints.measurementsAverage);
        expect(url.searchParams.get('quantity')).toEqual(Quantity.volume);
        expect(url.searchParams.get('before')).toBeTruthy();
        expect(url.searchParams.get('after')).toBeTruthy();
        expect(url.searchParams.get('city')).toBe(stockholm.id);
        expect(store.getActions().map(it => it.type)).toEqual([MEASUREMENT_REQUEST, MEASUREMENT_SUCCESS]);
      });

      it('make no average request when there is no user selections for given selection id', async () => {
        const aggregateItem: LegendItem = {
          ...toAggregateLegendItem({id: 999, name: 'bar'}),
          quantities: [Quantity.flow]
        };
        const legendItems: LegendItem[] = [aggregateItem];

        onFetchAsync(legendItems);

        await store.dispatch(fetchMeasurements({...parameters, legendItems}));

        expect(store.getActions()).toEqual([]);
      });

      it('filters out average readouts without values', async () => {
        const first: LegendItem = legendItemOf(Medium.districtHeating);
        const aggregateItem: LegendItem = {
          ...toAggregateLegendItem({id: 1, name: 'bar'}),
          quantities: [Quantity.volume, Quantity.flow]
        };
        const legendItems: LegendItem[] = [first, aggregateItem];

        onFetchAsync(legendItems);

        await store.dispatch(fetchMeasurements({...parameters, legendItems}));

        expect(store.getActions()).toEqual([
          measurementRequest(),
          measurementSuccess({
            measurements: [makeMeasurementResponse(first)],
            average: aggregateItem.quantities.map(_ => ({
              ...makeMeasurementAverageResponse(aggregateItem),
              values: justValues
            })),
            compare: [],
          }),
        ]);
      });

      it('does not fetch measurements when there are not items to fetch', async () => {
        const store = storeWith(initialState);

        onFetchAsync([legendItemOf(Medium.districtHeating), legendItemOf(Medium.districtHeating)]);

        await store.dispatch(fetchMeasurements(parameters));

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

        await store.dispatch(fetchMeasurements({...parameters, legendItems: [meter]}));
      };

    });

    const onFetchAsync = (legendItems: LegendItem[]): string[] => {
      const requestedUrls: string[] = [];
      const mockRestClient = new MockAdapter(axios);

      authenticate('test');

      mockRestClient.onGet().reply(async (config) => {
        requestedUrls.push(config.url!);

        if (config.url!.match(/^\/measurements\/average/)) {
          const average: MeasurementsApiResponse = legendItems
            .filter(it => isAggregate(it.type))
            .map(makeMeasurementAverageResponse);
          return [200, average];
        } else {
          const measurement: MeasurementsApiResponse = legendItems
            .filter(it => isMedium(it.type))
            .map(makeMeasurementResponse);
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

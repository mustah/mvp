import axios from 'axios';
import {default as MockAdapter} from 'axios-mock-adapter';
import {routerActions} from 'connected-react-router';
import {first, flatten, values as objectValues} from 'lodash';
import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {makeUser} from '../../../../../__tests__/testDataFactory';
import {routes} from '../../../../../app/routes';
import {Period, TemporalResolution} from '../../../../../components/dates/dateModels';
import {InvalidToken} from '../../../../../exceptions/InvalidToken';
import {idGenerator} from '../../../../../helpers/idGenerator';
import {Maybe} from '../../../../../helpers/Maybe';
import {initTranslations} from '../../../../../i18n/__tests__/i18nMock';
import {authenticate} from '../../../../../services/restClient';
import {toIdNamed} from '../../../../../types/Types';
import {logoutUser} from '../../../../../usecases/auth/authActions';
import {AuthState, Unauthorized} from '../../../../../usecases/auth/authModels';
import {toAggregateLegendItem} from '../../../../../usecases/report/helpers/legendHelper';
import {noInternetConnection, requestTimeout} from '../../../../api/apiActions';
import {NormalizedState} from '../../../../domain-models/domainModels';
import {initialDomain} from '../../../../domain-models/domainModelsReducer';
import {User} from '../../../../domain-models/user/userModels';
import {getQuantity} from '../../../../report/reportActions';
import {
  isAggregate,
  isKnownMedium,
  isMedium,
  LegendItem,
  LegendType,
  ReportSector
} from '../../../../report/reportModels';
import {ParameterName, UserSelection} from '../../../../user-selection/userSelectionModels';
import {initialState as initialUserSelectionState} from '../../../../user-selection/userSelectionReducer';
import {ToolbarView} from '../../../toolbar/toolbarModels';
import {
  exportReportToExcel,
  exportToExcelAction,
  fetchMeasurementsForReport,
  mapMediumToIds,
  measurementFailure,
  measurementRequest,
  measurementsRequestModelsOf,
  measurementSuccess,
  requestModelsByType
} from '../measurementActions';
import {
  allQuantitiesMap,
  getMediumText,
  MeasurementParameters,
  MeasurementResponsePart,
  MeasurementState,
  MeasurementValue,
  Medium,
  Quantity,
  quantityComparator
} from '../measurementModels';
import {initialState} from '../measurementReducer';

describe('measurementActions', () => {

  const configureMockStore = configureStore([thunk]);

  const storeWith = (measurement: MeasurementState, auth?: AuthState) => configureMockStore({measurement, auth});

  const legendItemOf = (type: LegendType, label: string = 'facility-1'): LegendItem => {
    const id = idGenerator.uuid().toString();
    const quantities = isKnownMedium(type) ? [getQuantity({type})] : [];
    return ({id, type, label, isHidden: false, quantities});
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

  const parameters: MeasurementParameters = {
    legendItems: [],
    reportDateRange: {period: Period.currentMonth},
    resolution: TemporalResolution.day,
    shouldComparePeriod: false,
    shouldShowAverage: false,
    view: ToolbarView.table,
  };

  let mockRestClient;

  beforeEach(() => {
    mockRestClient = new MockAdapter(axios);
    authenticate('test');
  });

  describe('fetchMeasurements', () => {

    initTranslations({
      code: 'en',
      translation: {
        test: 'no translations will default to key',
      },
    });

    const makeMeasurementResponse = ({id, label, quantities}: LegendItem) => ({
      id: id.toString(),
      label,
      quantity: first(quantities) || Quantity.power,
      medium: getMediumText(Medium.electricity),
      values: [{when: 1516521585107, value: 0.4353763591158477}],
      unit: 'mW',
    });

    const makeMeasurementAverageResponse = ({id, label, quantities}: LegendItem): MeasurementResponsePart =>
      ({id: id.toString(), label, quantity: first(quantities) || Quantity.flow, values, unit: 'mW'});

    describe('do not fetch', () => {

      it('should not dispatch any actions when nothing is pre-selected', async () => {
        const store = storeWith(initialState);

        await store.dispatch(fetchMeasurementsForReport(parameters) as any);

        expect(store.getActions()).toEqual([]);
      });

      it('should not fetch when already fetching', async () => {
        const store = storeWith({...initialState, isFetching: true});

        await store.dispatch(fetchMeasurementsForReport(parameters) as any);

        expect(store.getActions()).toEqual([]);
      });

      it('should not fetch when is has successfully fetched measurements', async () => {
        const store = storeWith({...initialState, isFetching: false, isSuccessfullyFetched: true});

        await store.dispatch(fetchMeasurementsForReport(parameters) as any);

        expect(store.getActions()).toEqual([]);
      });

      it('should not fetch when there is an error that is not cleared yet', async () => {
        const store = storeWith({...initialState, error: Maybe.just({message: 'error'})});

        await store.dispatch(fetchMeasurementsForReport(parameters) as any);

        expect(store.getActions()).toEqual([]);
      });
    });

    describe('only fetch quantities for meters that have them', () => {

      it('does not send requests for meters with unknown media', async () => {
        const store = storeWith(initialState);

        const legendItems = [legendItemOf(Medium.unknown), legendItemOf(Medium.unknown)];
        const measurementParameters: MeasurementParameters = {...parameters, legendItems};

        onFetchMeasurementsAsync(measurementParameters);

        await store.dispatch(fetchMeasurementsForReport(measurementParameters) as any);

        expect(store.getActions()).toEqual([]);
      });

      it('sends one request for selected legend item', async () => {
        const store = storeWith(initialState, {user: makeUser(), isAuthenticated: true});
        const roomSensorMeter = legendItemOf(Medium.roomSensor);
        const legendItems = [roomSensorMeter];
        const measurementParameters: MeasurementParameters = {...parameters, legendItems};

        onFetchMeasurementsAsync(measurementParameters);

        await store.dispatch(fetchMeasurementsForReport(measurementParameters) as any);

        expect(store.getActions()).toEqual([
          measurementRequest(ReportSector.report)(),
          measurementSuccess(ReportSector.report)({
            measurements: [
              makeMeasurementResponse(roomSensorMeter),
            ],
            average: [],
            compare: [],
          })
        ]);
      });

      it('sends separate requests for meters with different medium', async () => {
        const store = storeWith(initialState, {user: makeUser(), isAuthenticated: true});
        const roomSensorMeter = legendItemOf(Medium.roomSensor);
        const gasMeter = legendItemOf(Medium.gas, 'facility-gas');
        const legendItems = [roomSensorMeter, gasMeter];
        const measurementParameters: MeasurementParameters = {...parameters, legendItems};

        onFetchMeasurementsAsync(measurementParameters);

        await store.dispatch(fetchMeasurementsForReport(measurementParameters) as any);

        expect(store.getActions()).toEqual([
          measurementRequest(ReportSector.report)(),
          measurementSuccess(ReportSector.report)({
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
        store = configureMockStore({
          auth: {user: makeUser(), isAuthenticated: true},
          measurement: initialState,
          domainModels: {userSelections},
        });
      });

      it('make average request for user selection', async () => {
        const item: LegendItem = {...toAggregateLegendItem({id: 1, name: 'foo'}), quantities: [Quantity.volume]};
        const legendItems = [item];
        const measurementParameters: MeasurementParameters = {...parameters, legendItems};

        onFetchSelectionAverageAsync(measurementParameters);

        await store.dispatch(fetchMeasurementsForReport(measurementParameters));

        expect(store.getActions()).toEqual([
          measurementRequest(ReportSector.report)(),
          measurementSuccess(ReportSector.report)({
            average: item.quantities.map(_ => ({
              ...makeMeasurementAverageResponse(item),
              values: justValues
            })),
            measurements: [],
            compare: [],
          }),
        ]);
      });

      it('make no average request when there is no user selections for given selection id', async () => {
        const aggregateItem: LegendItem = {
          ...toAggregateLegendItem({id: 999, name: 'bar'}),
          quantities: [Quantity.flow]
        };
        const legendItems: LegendItem[] = [aggregateItem];
        const measurementParameters: MeasurementParameters = {...parameters, legendItems};

        onFetchSelectionAverageAsync(measurementParameters);

        await store.dispatch(fetchMeasurementsForReport(measurementParameters));

        expect(store.getActions()).toEqual([]);
      });

      it('filters out average readouts without values', async () => {
        const first: LegendItem = legendItemOf(Medium.districtHeating);
        const aggregateItem: LegendItem = {
          ...toAggregateLegendItem({id: 1, name: 'bar'}),
          quantities: [Quantity.volume, Quantity.flow],
        };
        const legendItems: LegendItem[] = [first, aggregateItem];
        const measurementParameters: MeasurementParameters = {...parameters, legendItems};

        onFetchMeasurementsAsync(measurementParameters);
        onFetchSelectionAverageAsync(measurementParameters);

        await store.dispatch(fetchMeasurementsForReport(measurementParameters));

        const measurementResponse = makeMeasurementResponse(first);
        expect(store.getActions()).toEqual([
          measurementRequest(ReportSector.report)(),
          measurementSuccess(ReportSector.report)({
            measurements: [
              {
                ...measurementResponse,
                ...makeMeasurementAverageResponse(first),
                values: measurementResponse.values
              }
            ],
            average: aggregateItem.quantities.map(_ => ({
              ...makeMeasurementAverageResponse(aggregateItem),
              values: justValues
            })),
            compare: [],
          }),
        ]);
      });

      it('makes meter average request for selected meters', async () => {
        const item: LegendItem = legendItemOf(Medium.districtHeating);
        const legendItems: LegendItem[] = [item];

        const measurementParameters: MeasurementParameters = {...parameters, legendItems, shouldShowAverage: true};

        onFetchMeasurementsAsync(measurementParameters);

        await store.dispatch(fetchMeasurementsForReport(measurementParameters));

        expect(store.getActions()).toEqual([
          measurementRequest(ReportSector.report)(),
          measurementSuccess(ReportSector.report)({
            average: [{...makeMeasurementAverageResponse(item), values: justValues}],
            measurements: [makeMeasurementAverageResponse(item)],
            compare: [],
          }),
        ]);
      });

      it('does not fetch measurements when there are not items to fetch', async () => {
        const store = storeWith(initialState);
        const legendItems = [];
        const measurementParameters: MeasurementParameters = {...parameters, legendItems};

        onFetchMeasurementsAsync(measurementParameters);

        await store.dispatch(fetchMeasurementsForReport(measurementParameters) as any);

        expect(store.getActions()).toEqual([]);
      });

      describe('make measurement uri parameters', () => {

        it('makes request for meters that have selected quantities', () => {
          const medium = Medium.districtHeating;
          const quantities = allQuantitiesMap[medium];
          const item1: LegendItem = legendItemOf(medium, 'a');
          const item2: LegendItem = {...legendItemOf(medium, 'b'), quantities};
          const legendItems: LegendItem[] = [item1, item2];
          const measurementParameters: MeasurementParameters = {...parameters, legendItems};

          expect(measurementsRequestModelsOf(measurementParameters)).toHaveLength(1);
        });

        it('makes request for meters that have selected quantities and with different medium', () => {
          const item1: LegendItem = legendItemOf(Medium.districtHeating, 'a');
          const item2: LegendItem = legendItemOf(Medium.roomSensor, 'b');
          const legendItems: LegendItem[] = [item1, item2];

          const requestModels = measurementsRequestModelsOf({...parameters, legendItems});

          expect(requestModels).toHaveLength(2);
          expect(requestModels[0].quantity).toEqual([
            'Energy::consumption',
            'Volume::consumption',
            'Flow::readout',
            'Power::readout',
            'Forward temperature::readout',
            'Return temperature::readout',
            'Difference temperature::readout'
          ]);
          expect(requestModels[1].quantity).toEqual([
            'External temperature::readout',
            'Relative humidity::readout'
          ]);
        });
      });

    });

    const onFetchMeasurementsAsync = (parameters: MeasurementParameters): void => {
      measurementsRequestModelsOf(parameters)
        .map(_ => mockRestClient.onPost().reply(async (config) => {
          const {legendItems, shouldShowAverage} = parameters;
          const url = config.url!.replace(config.baseURL!, '');

          if (url.match(/^\/measurements\/average/) || shouldShowAverage) {
            return [
              200,
              legendItems.filter(_ => shouldShowAverage).map(makeMeasurementAverageResponse)
            ];
          } else {
            return [200, legendItems.filter(isMedium).map(makeMeasurementResponse)];
          }
        }));
    };

    const onFetchSelectionAverageAsync = ({legendItems}: MeasurementParameters): void => {
      mockRestClient.onGet().reply(async () => [
        200,
        legendItems.filter(it => isAggregate(it.type)).map(makeMeasurementAverageResponse)
      ]);
    };

  });

  describe('requestModelsByType', () => {

    it('makes meter average request for selected quantities for each meter type', async () => {
      const item1: LegendItem = {
        ...legendItemOf(Medium.districtHeating),
        label: 'a',
        quantities: [Quantity.volume]
      };
      const item2: LegendItem = {...legendItemOf(Medium.districtHeating), label: 'b', quantities: [Quantity.flow]};
      const item3: LegendItem = {...legendItemOf(Medium.gas), label: 'c', quantities: [Quantity.volume]};
      const legendItems: LegendItem[] = [item1, item2, item3];

      const measurementParameters: MeasurementParameters = {...parameters, legendItems};

      expect(requestModelsByType(measurementParameters)).toHaveLength(2);
    });

  });

  describe('handle request errors', () => {
    let store;

    beforeEach(() => {
      store = storeWith(initialState);
    });

    it('logs out user when token is invalid', async () => {
      const user: User = makeUser();
      store = storeWith(initialState, {user, isAuthenticated: true});
      const error = new InvalidToken('Token missing or invalid');

      (() => mockRestClient.onPost().reply(async () => [401, error]))();

      await onFetchMeasurements();

      expect(store.getActions()).toEqual([
        measurementRequest(ReportSector.report)(),
        logoutUser(error as Unauthorized),
        routerActions.push(`${routes.login}/${user.organisation.slug}`),
      ]);
    });

    it('handles request timeouts', async () => {
      (() => mockRestClient.onPost().timeout())();

      await onFetchMeasurements();

      expect(store.getActions()).toEqual([
        measurementRequest(ReportSector.report)(),
        measurementFailure(ReportSector.report)(Maybe.just(requestTimeout()))
      ]);
    });

    it('handles network errors', async () => {
      (() => mockRestClient.onPost().networkError())();

      await onFetchMeasurements();

      expect(store.getActions()).toEqual([
        measurementRequest(ReportSector.report)(),
        measurementFailure(ReportSector.report)(Maybe.just(noInternetConnection()))
      ]);
    });

    it('handles custom error messages', async () => {
      const response = {message: 'Error'};
      (() => mockRestClient.onPost().reply(() => [500, response]))();

      await onFetchMeasurements();

      expect(store.getActions()).toEqual([
        measurementRequest(ReportSector.report)(),
        measurementFailure(ReportSector.report)(Maybe.maybe(response))
      ]);
    });

    const onFetchMeasurements = async () => {
      const meter = legendItemOf(Medium.districtHeating);

      await store.dispatch(fetchMeasurementsForReport({...parameters, legendItems: [meter]}));
    };
  });

  describe('exportReportToExcel', () => {

    it('dispatches action if no export is ongoing', () => {
      const store = storeWith(initialState);

      store.dispatch(exportReportToExcel() as any);

      expect(store.getActions()).toEqual([exportToExcelAction(ReportSector.report)()]);
    });

    it('does not dispatch action if export is ongoing', () => {
      const store = storeWith({
        ...initialState,
        isExportingToExcel: true,
      });

      store.dispatch(exportReportToExcel() as any);

      expect(store.getActions()).toEqual([]);
    });

  });

  describe('mapMediumToIds', () => {

    it('contains all mediums, formatted', () => {
      expect(Object.keys(mapMediumToIds([]))).toEqual(Object.keys(Medium));
    });

    it('groups legend items together', () => {
      const firstId = idGenerator.uuid();
      const secondId = idGenerator.uuid();
      const legendItems: LegendItem[] = [
        {
          id: firstId,
          quantities: [
            Quantity.differenceTemperature,
            Quantity.energy
          ],
          label: '_',
          type: Medium.districtHeating,
        },
        {
          id: secondId,
          quantities: [
            Quantity.differenceTemperature,
          ],
          label: '_',
          type: Medium.districtHeating,
        },
      ];

      expect(mapMediumToIds(legendItems)[Medium.districtHeating]).toEqual([firstId, secondId]);
    });

    it('ignores empty medium', () => {
      const legendItems: LegendItem[] = [
        {
          id: idGenerator.uuid(),
          quantities: [
            Quantity.differenceTemperature,
            Quantity.energy
          ],
          label: '_',
          type: undefined as unknown as Medium,
        },
      ];

      expect(flatten(objectValues(mapMediumToIds(legendItems)))).toEqual([]);
    });

  });

  describe('sorted quantities', () => {

    it('sorts using quantity comparator', () => {
      expect([Quantity.power, Quantity.flow].sort(quantityComparator)).toEqual([Quantity.flow, Quantity.power]);

      expect([
        Quantity.energy,
        Quantity.differenceTemperature,
        Quantity.flow,
      ].sort(quantityComparator)).toEqual([
        Quantity.energy,
        Quantity.flow,
        Quantity.differenceTemperature,
      ]);

      expect([
        Quantity.differenceTemperature,
        Quantity.energy,
        Quantity.power,
        Quantity.flow,
        Quantity.volume,
      ].sort(quantityComparator)).toEqual([
        Quantity.energy,
        Quantity.volume,
        Quantity.flow,
        Quantity.power,
        Quantity.differenceTemperature,
      ]);

      expect([
        Quantity.forwardTemperature,
        Quantity.volume,
        Quantity.returnTemperature,
        Quantity.energy,
        Quantity.power,
        Quantity.flow,
        Quantity.differenceTemperature,
      ].sort(quantityComparator)).toEqual([
        Quantity.energy,
        Quantity.volume,
        Quantity.flow,
        Quantity.power,
        Quantity.forwardTemperature,
        Quantity.returnTemperature,
        Quantity.differenceTemperature,
      ]);

    });
  });

});

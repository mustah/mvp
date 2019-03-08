import {find, flatMap, groupBy, map, sortBy} from 'lodash';
import {createAction, createStandardAction} from 'typesafe-actions';
import {EmptyAction, PayloadAction} from 'typesafe-actions/dist/types';
import {DateRange, Period, TemporalResolution} from '../../../../components/dates/dateModels';
import {InvalidToken} from '../../../../exceptions/InvalidToken';
import {isDefined} from '../../../../helpers/commonHelpers';
import {makeCompareCustomDateRange, makeCompareDateRange} from '../../../../helpers/dateHelpers';
import {Maybe} from '../../../../helpers/Maybe';
import {encodeRequestParameters, makeUrl, requestParametersFrom} from '../../../../helpers/urlFactory';
import {GetState} from '../../../../reducers/rootReducer';
import {EndPoints} from '../../../../services/endPoints';
import {isTimeoutError, restClient, wasRequestCanceled} from '../../../../services/restClient';
import {emptyActionOf, EncodedUriParameters, ErrorResponse, payloadActionOf, uuid} from '../../../../types/Types';
import {logout} from '../../../../usecases/auth/authActions';
import {isAggregate, isMedium, LegendType} from '../../../../usecases/report/reportModels';
import {FetchIfNeeded, noInternetConnection, requestTimeout, responseMessageOrFallback} from '../../../api/apiActions';
import {getDomainModelById} from '../../../domain-models/domainModelsSelectors';
import {SelectionInterval, UserSelection} from '../../../user-selection/userSelectionModels';
import {
  getGroupHeaderTitle,
  MeasurementParameters,
  MeasurementResponse,
  MeasurementResponsePart,
  MeasurementsApiResponse,
  MeasurementState,
  Quantity
} from './measurementModels';

export const measurementRequest = createAction('MEASUREMENT_REQUEST');
export const measurementSuccess = createStandardAction('MEASUREMENT_SUCCESS')<MeasurementResponse>();
export const measurementFailure = createStandardAction('MEASUREMENT_FAILURE')<Maybe<ErrorResponse>>();
export const measurementClearError = createAction('MEASUREMENT_CLEAR_ERROR');

export const exportToExcelAction = createAction('EXPORT_TO_EXCEL');
export const exportToExcelSuccess = createAction('EXPORT_TO_EXCEL_SUCCESS');

export const meterDetailMeasurementRequest =
  createAction('METER_DETAIL_MEASUREMENT_REQUEST');

export const METER_DETAIL_MEASUREMENT_SUCCESS = 'METER_DETAIL_MEASUREMENT_SUCCESS';

export const meterDetailMeasurementSuccess =
  payloadActionOf<MeasurementResponse>(METER_DETAIL_MEASUREMENT_SUCCESS);

export const METER_DETAIL_MEASUREMENT_FAILURE = 'METER_DETAIL_MEASUREMENT_FAILURE';
export const meterDetailMeasurementFailure =
  payloadActionOf<Maybe<ErrorResponse>>(METER_DETAIL_MEASUREMENT_FAILURE);

export const METER_DETAIL_MEASUREMENT_CLEAR_ERROR = 'METER_DETAIL_MEASUREMENT_CLEAR_ERROR';
export const meterDetailMeasurementClearError =
  emptyActionOf(METER_DETAIL_MEASUREMENT_CLEAR_ERROR);

export const meterDetailExportToExcelAction = createAction('METER_DETAIL_EXPORT_TO_EXCEL');

export const METER_DETAIL_EXPORT_TO_EXCEL_SUCCESS = 'METER_DETAIL_EXPORT_TO_EXCEL_SUCCESS';
export const meterDetailExportToExcelSuccess = emptyActionOf(METER_DETAIL_EXPORT_TO_EXCEL_SUCCESS);

export const exportToExcel = () =>
  (dispatch, getState: GetState) => {
    if (!getState().measurement.isExportingToExcel) {
      dispatch(exportToExcelAction());
    }
  };

const measurementMeterUri = (
  quantity: Quantity,
  resolution: TemporalResolution,
  dateRange: SelectionInterval,
  meterIds: uuid[],
  label: string,
): EncodedUriParameters =>
  encodeRequestParameters({
    ...requestParametersFrom({dateRange}),
    label,
    quantity,
    resolution,
    logicalMeterId: meterIds.map(id => id.toString()),
  });

interface LabelItem {
  quantity: Quantity;
  type: LegendType;
}

interface GraphDataResponse {
  data: MeasurementsApiResponse;
}

type GraphDataRequests = Array<Promise<GraphDataResponse>>;

type QuantityToIds = { [q in Quantity]: uuid[] };

const makeQuantityToIdsMap = (): QuantityToIds =>
  Object.keys(Quantity)
    .map(k => Quantity[k])
    .reduce((acc, quantity) => ({...acc, [quantity]: []}), {});

export const meterLabelFactory = ({quantity}): string => quantity;

export const meterAverageLabelFactory = ({quantity, type}): string =>
  `${getGroupHeaderTitle(type)}`;

export const metersByQuantityRequests = (parameters: MeasurementParameters): GraphDataRequests =>
  makeMeasurementMetersUriParameters(parameters, EndPoints.measurements, meterLabelFactory)
    .map(url => restClient.getParallel(url));

export const urlsByType = (parameters: MeasurementParameters): EncodedUriParameters[] => {
  const byType = groupBy(parameters.legendItems, it => it.type);
  const legendItemsParameters = flatMap(
    Object.keys(byType)
      .map(type => byType[type])
      .map(legendItems => ({...parameters, legendItems})));
  return flatMap(legendItemsParameters
    .map(p => makeMeasurementMetersUriParameters(p, EndPoints.measurementsAverage, meterAverageLabelFactory)));
};

export const makeMeasurementMetersUriParameters = (
  {
    dateRange,
    legendItems,
    resolution,
  }: MeasurementParameters,
  endpoint: EndPoints,
  labelFactory: (labelItem: LabelItem) => string,
): EncodedUriParameters[] => {
  const quantityToIds = makeQuantityToIdsMap();

  const items = legendItems.filter(it => isMedium(it.type));

  flatMap(items, it => it.quantities.forEach(q => quantityToIds[q].push(it.id)));

  return Object.keys(quantityToIds)
    .filter((quantity: Quantity) => quantityToIds[quantity].length > 0)
    .map((quantity: Quantity) => {
      const {type} = find(legendItems, {id: quantityToIds[quantity][0]})!;
      return measurementMeterUri(
        quantity,
        resolution,
        dateRange,
        quantityToIds[quantity],
        labelFactory({type, quantity})
      );
    })
    .map(parameters => makeUrl(endpoint, parameters));
};

const averageForSelectedMetersRequests = (parameters: MeasurementParameters): GraphDataRequests => {
  if (parameters.shouldShowAverage) {
    return urlsByType(parameters).map(url => restClient.getParallel(url));
  } else {
    return [];
  }
};

const compareMeterRequests = (parameters: MeasurementParameters): GraphDataRequests =>
  Maybe.maybe<MeasurementParameters>(parameters)
    .filter(it => it.shouldComparePeriod)
    .map(it => {
        const {period, customDateRange} = it.dateRange;
        const dateRange = Maybe.maybe<DateRange>(customDateRange)
          .map(makeCompareCustomDateRange)
          .orElseGet(() => makeCompareDateRange(period));
        return metersByQuantityRequests({
          ...it,
          dateRange: {period: Period.custom, customDateRange: dateRange}
        });
      }
    ).orElse([]);

const averageForUserSelectionsRequests = (
  {
    dateRange,
    legendItems,
    resolution
  }: MeasurementParameters,
  getState: GetState,
): GraphDataRequests => {
  const quantityToIds = makeQuantityToIdsMap();

  const aggregateItems = legendItems.filter(it => isAggregate(it.type));

  flatMap(aggregateItems, it => it.quantities.forEach(q => quantityToIds[q].push(it.id)));

  const rootState = getState();

  const parameters = Object.keys(quantityToIds)
    .filter((q: Quantity) => quantityToIds[q].length > 0)
    .map((quantity: Quantity) =>
      quantityToIds[quantity]
        .map(id => getDomainModelById<UserSelection>(id)(rootState.domainModels.userSelections).getOrElseUndefined())
        .filter(isDefined)
        .map((it: UserSelection) => ({
          ...requestParametersFrom({...it.selectionParameters, dateRange}),
          quantity,
          resolution,
          label: it.name,
        }))
        .map(encodeRequestParameters));

  return flatMap(parameters)
    .map(parameter => makeUrl(EndPoints.measurementsAverage, parameter))
    .map(url => restClient.getParallel(url));
};

const removeUndefinedValues = (averageEntity: MeasurementResponsePart): MeasurementResponsePart => ({
  ...averageEntity,
  values: averageEntity.values.filter(({value}) => value !== undefined),
});

const shouldFetchMeasurementsReport: FetchIfNeeded = (getState: GetState): boolean => {
  const {isFetching, isSuccessfullyFetched, error}: MeasurementState = getState().measurement;
  return !isSuccessfullyFetched && !isFetching && error.isNothing();
};

const shouldFetchMeasurementsMeterDetails: FetchIfNeeded = (getState: GetState): boolean => {
  const {isFetching, isSuccessfullyFetched, error}: MeasurementState =
    getState().domainModels.meterDetailMeasurement;
  return !isSuccessfullyFetched && !isFetching && error.isNothing();
};

interface RequestHandler {
  request: () => EmptyAction<string>;
  success: (payload: MeasurementResponse) => PayloadAction<string, MeasurementResponse>;
  failure: (payload: Maybe<ErrorResponse>) => PayloadAction<string, Maybe<ErrorResponse>>;
}

const fetchMeasurements = (
  parameters: MeasurementParameters,
  {request, success, failure}: RequestHandler,
  fetchIfNeeded: FetchIfNeeded
) =>
  async (dispatch, getState: GetState) => {
    if (fetchIfNeeded(getState)) {
      const meters: GraphDataRequests = metersByQuantityRequests(parameters);
      const meterAverage: GraphDataRequests = averageForSelectedMetersRequests(parameters);
      const average: GraphDataRequests = averageForUserSelectionsRequests(parameters, getState);
      const compare: GraphDataRequests = compareMeterRequests(parameters);

      if (meters.length || meterAverage.length || average.length || compare.length) {
        dispatch(measurementRequest());
        try {
          const [meterResponses, meterAverageResponses, averageResponses, compareResponses]: GraphDataResponse[][] =
            await Promise.all([
              Promise.all(meters),
              Promise.all(meterAverage),
              Promise.all(average),
              Promise.all(compare)
            ]);

          const meterAverages = map(flatMap(meterAverageResponses, 'data'), removeUndefinedValues);
          const userSelectionAverages = map(flatMap(averageResponses, 'data'), removeUndefinedValues);

          const response: MeasurementResponse = {
            average: [...meterAverages, ...userSelectionAverages],
            compare: sortBy(flatMap(compareResponses, 'data'), 'label'),
            measurements: sortBy(flatMap(meterResponses, 'data'), 'label'),
          };
          dispatch(success(response));
        } catch (error) {
          if (error instanceof InvalidToken) {
            await dispatch(logout(error));
          } else if (wasRequestCanceled(error)) {
            return;
          } else if (isTimeoutError(error)) {
            dispatch(failure(Maybe.maybe(requestTimeout())));
          } else if (!error.response) {
            dispatch(failure(Maybe.maybe(noInternetConnection())));
          } else {
            dispatch(failure(Maybe.maybe(responseMessageOrFallback(error.response))));
          }
        }
      }
    }
  };

export const fetchMeasurementsForMeterDetails = (measurementParameters: MeasurementParameters) =>
  fetchMeasurements(
    measurementParameters,
    {
      failure: meterDetailMeasurementFailure,
      request: meterDetailMeasurementRequest,
      success: meterDetailMeasurementSuccess
    },
    shouldFetchMeasurementsMeterDetails
  );

export const fetchMeasurementsForReport = (measurementParameters: MeasurementParameters) =>
  fetchMeasurements(
    measurementParameters,
    {
      failure: measurementFailure,
      request: measurementRequest,
      success: measurementSuccess
    },
    shouldFetchMeasurementsReport
  );

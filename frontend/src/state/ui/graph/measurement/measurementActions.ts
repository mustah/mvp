import {flatMap, map, sortBy} from 'lodash';
import {createAction} from 'typesafe-actions';
import {EmptyAction, PayloadAction} from 'typesafe-actions/dist/types';
import {DateRange, Period, TemporalResolution} from '../../../../components/dates/dateModels';
import {InvalidToken} from '../../../../exceptions/InvalidToken';
import {isDefined} from '../../../../helpers/commonHelpers';
import {makeCompareCustomDateRange, makeCompareDateRange} from '../../../../helpers/dateHelpers';
import {Maybe} from '../../../../helpers/Maybe';
import {
  encodeRequestParameters,
  makeUrl,
  requestParametersFrom
} from '../../../../helpers/urlFactory';
import {GetState} from '../../../../reducers/rootReducer';
import {EndPoints} from '../../../../services/endPoints';
import {isTimeoutError, restClient, wasRequestCanceled} from '../../../../services/restClient';
import {
  emptyActionOf,
  EncodedUriParameters,
  ErrorResponse,
  payloadActionOf,
  uuid
} from '../../../../types/Types';
import {logout} from '../../../../usecases/auth/authActions';
import {isAggregate, isMedium} from '../../../../usecases/report/reportModels';
import {FetchIfNeeded, noInternetConnection, requestTimeout, responseMessageOrFallback} from '../../../api/apiActions';
import {getDomainModelById} from '../../../domain-models/domainModelsSelectors';
import {SelectedParameters, UserSelection} from '../../../user-selection/userSelectionModels';
import {
  MeasurementParameters,
  MeasurementResponse,
  MeasurementResponsePart,
  MeasurementsApiResponse,
  MeasurementState,
  Quantity
} from './measurementModels';

export const MEASUREMENT_REQUEST = 'MEASUREMENT_REQUEST';
export const measurementRequest = createAction(MEASUREMENT_REQUEST);

export const MEASUREMENT_SUCCESS = 'MEASUREMENT_SUCCESS';
export const measurementSuccess = payloadActionOf<MeasurementResponse>(MEASUREMENT_SUCCESS);

export const MEASUREMENT_FAILURE = 'MEASUREMENT_FAILURE';
export const measurementFailure = payloadActionOf<Maybe<ErrorResponse>>(MEASUREMENT_FAILURE);

export const MEASUREMENT_CLEAR_ERROR = 'MEASUREMENT_CLEAR_ERROR';
export const measurementClearError = emptyActionOf(MEASUREMENT_CLEAR_ERROR);

export const exportToExcelAction = createAction('EXPORT_TO_EXCEL');

export const EXPORT_TO_EXCEL_SUCCESS = 'EXPORT_TO_EXCEL_SUCCESS';
export const exportToExcelSuccess = emptyActionOf(EXPORT_TO_EXCEL_SUCCESS);

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
  meterIds: uuid[],
  {dateRange}: SelectedParameters,
): EncodedUriParameters =>
  encodeRequestParameters({
    ...requestParametersFrom({dateRange}),
    quantity,
    resolution,
    logicalMeterId: meterIds.map(id => id.toString()),
  });

interface GraphDataResponse {
  data: MeasurementsApiResponse;
}

type GraphDataRequests = Array<Promise<GraphDataResponse>>;

type QuantityToIds = { [q in Quantity]: uuid[] };

const makeQuantityToIdsMap = (): QuantityToIds =>
  Object.keys(Quantity)
    .map(k => Quantity[k])
    .reduce((acc, quantity) => ({...acc, [quantity]: []}), {});

const metersByQuantityRequests = ({
  legendItems,
  resolution,
  selectionParameters,
}: MeasurementParameters): GraphDataRequests => {
  const quantityToIds = makeQuantityToIdsMap();

  const metersItems = legendItems.filter(it => isMedium(it.type));

  flatMap(metersItems, it => it.quantities.forEach(q => quantityToIds[q].push(it.id)));

  return Object.keys(quantityToIds).filter((q: Quantity) => quantityToIds[q].length > 0)
    .map((q: Quantity) => measurementMeterUri(q, resolution, quantityToIds[q], selectionParameters))
    .map(parameters => restClient.getParallel(makeUrl(EndPoints.measurements, parameters)));
};

const compareMeterRequests = (parameters: MeasurementParameters): GraphDataRequests =>
  Maybe.maybe<MeasurementParameters>(parameters)
    .filter(it => it.shouldComparePeriod)
    .map(it => {
        const {period, customDateRange} = it.selectionParameters.dateRange;
        const dateRange = Maybe.maybe<DateRange>(customDateRange)
          .map(makeCompareCustomDateRange)
          .orElseGet(() => makeCompareDateRange(period));
        return metersByQuantityRequests({
          ...it,
          selectionParameters: {
            ...it.selectionParameters,
            dateRange: {period: Period.custom, customDateRange: dateRange}
          }
        });
      }
    ).orElse([]);

const averageByQuantityRequests = (
  {
    legendItems,
    resolution,
    selectionParameters: {dateRange}
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

  return flatMap(parameters).map(p => restClient.getParallel(makeUrl(EndPoints.measurementsAverage, p)));
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
  fetchIfNeeded: FetchIfNeeded) =>
  async (dispatch, getState: GetState) => {
    if (fetchIfNeeded(getState)) {
      const meters: GraphDataRequests = metersByQuantityRequests(parameters);
      const average: GraphDataRequests = averageByQuantityRequests(parameters, getState);
      const compare: GraphDataRequests = compareMeterRequests(parameters);

      if (meters.length || average.length || compare.length) {
        dispatch(measurementRequest());
        try {
          const [meterResponses, averageResponses, compareResponses]: GraphDataResponse[][] =
            await Promise.all([Promise.all(meters), Promise.all(average), Promise.all(compare)]);

          const response: MeasurementResponse = {
            average: map(flatMap(averageResponses, 'data'), removeUndefinedValues),
            compare: sortBy(flatMap(compareResponses, 'data'), removeUndefinedValues, 'label'),
            measurements: sortBy(flatMap(meterResponses, 'data'), removeUndefinedValues, 'label'),
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

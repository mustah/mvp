import {flatMap, map, sortBy} from 'lodash';
import {createAction} from 'typesafe-actions';
import {TemporalResolution} from '../../../../components/dates/dateModels';
import {InvalidToken} from '../../../../exceptions/InvalidToken';
import {Maybe} from '../../../../helpers/Maybe';
import {
  encodeRequestParameters,
  makeApiParametersOf,
  makeUrl,
  requestParametersFrom
} from '../../../../helpers/urlFactory';
import {GetState} from '../../../../reducers/rootReducer';
import {EndPoints} from '../../../../services/endPoints';
import {isTimeoutError, restClient, wasRequestCanceled} from '../../../../services/restClient';
import {
  CallbackWith,
  emptyActionOf,
  EncodedUriParameters,
  ErrorResponse,
  payloadActionOf,
  uuid
} from '../../../../types/Types';
import {logout} from '../../../../usecases/auth/authActions';
import {OnLogout} from '../../../../usecases/auth/authModels';
import {FetchIfNeeded, noInternetConnection, requestTimeout, responseMessageOrFallback} from '../../../api/apiActions';
import {NormalizedPaginated} from '../../../domain-models-paginated/paginatedDomainModels';
import {SelectedParameters, SelectionInterval} from '../../../user-selection/userSelectionModels';
import {
  initialMeterMeasurementsState,
  Measurement,
  MeasurementApiResponse,
  MeasurementParameters,
  MeasurementResponse,
  MeasurementResponsePart,
  MeasurementState,
  MeterMeasurementsState,
  Quantity
} from './measurementModels';
import {measurementDataFormatter} from './measurementSchema';

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
    logicalMeterId: meterIds.map((id: uuid): string => id.toString()),
  });

interface GraphDataResponse {
  data: MeasurementApiResponse;
}

type GraphDataRequests = Array<Promise<GraphDataResponse>>;

interface GroupedRequests {
  average: GraphDataRequests;
  meters: GraphDataRequests;
}

type QuantityToMeterIds = { [q in Quantity]: uuid[] };

const makeQuantityToMeterIdsMap = (): QuantityToMeterIds =>
  Object.keys(Quantity)
    .map(k => Quantity[k])
    .reduce((acc, quantity) => ({...acc, [quantity]: []}), {});

const requestsPerQuantity = ({
  resolution,
  selectedReportItems: {meters},
  selectionParameters,
}: MeasurementParameters): GroupedRequests => {
  const requests: GroupedRequests = {average: [], meters: []};
  const quantityToMeterIds = makeQuantityToMeterIdsMap();

  flatMap(meters, it => it.quantities.forEach(q => quantityToMeterIds[q].push(it.id)));

  requests.meters = Object.keys(quantityToMeterIds)
    .filter((q: Quantity) => quantityToMeterIds[q].length > 0)
    .map((quantity: Quantity) =>
      restClient.getParallel(makeUrl(
        EndPoints.measurements,
        measurementMeterUri(quantity, resolution, quantityToMeterIds[quantity], selectionParameters),
      )));

  // TODO[!must!] fix average in next issue!!!
  //
  //   requests.average = Object.keys(meterByQuantity)
  //     .map((quantity: Quantity) =>
  //       restClient.getParallel(makeUrl(
  //         EndPoints.measurements.concat('/average'),
  //         measurementMeterUri(quantity, resolution, Array.from(meterByQuantity[quantity]!), selectionParameters),
  //       )));

  return requests;
};

const removeUndefinedValues = (averageEntity: MeasurementResponsePart): MeasurementResponsePart => ({
  ...averageEntity,
  values: averageEntity.values.filter(({value}) => value !== undefined),
});

const shouldFetchMeasurements: FetchIfNeeded = (getState: GetState): boolean => {
  const {isFetching, isSuccessfullyFetched, error}: MeasurementState = getState().measurement;
  return !isSuccessfullyFetched && !isFetching && error.isNothing();
};

export const fetchMeasurements = (measurementParameters: MeasurementParameters) =>
  async (dispatch, getState: GetState) => {
    if (shouldFetchMeasurements(getState)) {
      const {average, meters}: GroupedRequests = requestsPerQuantity(measurementParameters);

      if (meters.length) {
        dispatch(measurementRequest());
        try {
          const [meterResponses, averageResponses]: GraphDataResponse[][] =
            await Promise.all([Promise.all(meters), Promise.all(average)]);

          const response: MeasurementResponse = {
            measurements: sortBy(flatMap(meterResponses, 'data'), 'label'),
            average: map(
              flatMap(averageResponses, 'data'),
              removeUndefinedValues,
            ),
          };
          dispatch(measurementSuccess(response));
        } catch (error) {
          if (error instanceof InvalidToken) {
            await dispatch(logout(error));
          } else if (wasRequestCanceled(error)) {
            return;
          } else if (isTimeoutError(error)) {
            dispatch(measurementFailure(Maybe.maybe(requestTimeout())));
          } else if (!error.response) {
            dispatch(measurementFailure(Maybe.maybe(noInternetConnection())));
          } else {
            dispatch(measurementFailure(Maybe.maybe(responseMessageOrFallback(error.response))));
          }
        }
      }
    }
  };

interface MeasurementPagedApiResponse {
  data: NormalizedPaginated<Measurement>;
}

export const fetchMeasurementsPaged = async (
  id: uuid,
  selectionInterval: SelectionInterval,
  updateCallback: CallbackWith<MeterMeasurementsState>,
  logout: OnLogout
): Promise<void> => {
  try {
    const period = makeApiParametersOf(selectionInterval);
    const measurementUrl: EncodedUriParameters = makeUrl(
      EndPoints.measurementsPaged,
      `sort=created,desc&sort=quantity,asc&logicalMeterId=${id}&${period}`,
    );
    const {data}: MeasurementPagedApiResponse = await restClient.get(measurementUrl);

    updateCallback({
      ...initialMeterMeasurementsState,
      measurementPages: measurementDataFormatter(data),
    });
  } catch (error) {
    if (error instanceof InvalidToken) {
      await logout(error);
    } else if (wasRequestCanceled(error)) {
      return;
    } else if (isTimeoutError(error)) {
      updateCallback({...initialMeterMeasurementsState, error: Maybe.maybe(requestTimeout())});
    } else if (!error.response) {
      updateCallback({...initialMeterMeasurementsState, error: Maybe.maybe(noInternetConnection())});
    } else {
      updateCallback({
        ...initialMeterMeasurementsState,
        error: Maybe.maybe(responseMessageOrFallback(error.response)),
      });
    }
  }
};

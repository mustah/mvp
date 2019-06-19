import {find, flatMap, groupBy, map, sortBy} from 'lodash';
import {createAction, createStandardAction} from 'typesafe-actions';
import {EmptyAction, PayloadAction} from 'typesafe-actions/dist/type-helpers';
import {DateRange, Period, TemporalResolution} from '../../../../components/dates/dateModels';
import {InvalidToken} from '../../../../exceptions/InvalidToken';
import {isDefined} from '../../../../helpers/commonHelpers';
import {makeCompareCustomDateRange, makeCompareDateRange} from '../../../../helpers/dateHelpers';
import {Maybe} from '../../../../helpers/Maybe';
import {
  encodeRequestParameters,
  makeReportPeriodParametersOf,
  makeUrl,
  requestParametersFrom
} from '../../../../helpers/urlFactory';
import {GetState} from '../../../../reducers/rootReducer';
import {EndPoints} from '../../../../services/endPoints';
import {isTimeoutError, restClient, wasRequestCanceled} from '../../../../services/restClient';
import {EncodedUriParameters, ErrorResponse, uuid} from '../../../../types/Types';
import {logout} from '../../../../usecases/auth/authActions';
import {
  meterDetailMeasurementFailure,
  meterDetailMeasurementRequest,
  meterDetailMeasurementSuccess
} from '../../../../usecases/meter/measurements/meterDetailMeasurementActions';
import {FetchIfNeeded, noInternetConnection, requestTimeout, responseMessageOrFallback} from '../../../api/apiActions';
import {getDomainModelById} from '../../../domain-models/domainModelsSelectors';
import {isAggregate, isMedium, LegendItem, LegendType, ReportSector} from '../../../report/reportModels';
import {SelectionInterval, UserSelection} from '../../../user-selection/userSelectionModels';
import {
  getGroupHeaderTitle,
  MeasurementParameters,
  MeasurementResponse,
  MeasurementResponsePart,
  MeasurementsApiResponse,
  MeasurementState,
  Quantity,
  quantityAttributes,
  QuantityDisplayMode
} from './measurementModels';

export const measurementRequest = (sector: ReportSector) =>
  createAction(`MEASUREMENT_REQUEST_${sector}`);

export const measurementSuccess = (sector: ReportSector) =>
  createStandardAction(`MEASUREMENT_SUCCESS_${sector}`)<MeasurementResponse>();

export const measurementFailure = (sector: ReportSector) =>
  createStandardAction(`MEASUREMENT_FAILURE_${sector}`)<Maybe<ErrorResponse>>();

export const measurementClearError = (sector: ReportSector) =>
  createAction(`MEASUREMENT_CLEAR_ERROR_${sector}`);

export const exportToExcelAction = (sector: ReportSector) =>
  createAction(`EXPORT_TO_EXCEL_${sector}`);

export const exportToExcelSuccess = (sector: ReportSector) =>
  createAction(`EXPORT_TO_EXCEL_SUCCESS_${sector}`);

const measurementMeterUri = (
  quantity: Quantity,
  resolution: TemporalResolution,
  dateRange: SelectionInterval,
  meterIds: uuid[],
  label: string,
  displayMode: QuantityDisplayMode,
): EncodedUriParameters => {
  const quantityWithParams = quantity + '::' + displayMode;

  return `${makeReportPeriodParametersOf(dateRange)}&${encodeRequestParameters({
    label,
    quantity: quantityWithParams,
    resolution,
    logicalMeterId: meterIds.map(id => id.toString()),
  })}`;
};

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

export const meterAverageLabelFactory = ({type}): string => `${getGroupHeaderTitle(type)}`;

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

export const groupLegendItemsByQuantity = (items: LegendItem[]): QuantityToIds =>
  items
    .filter(it => isMedium(it.type))
    .reduce(
      (acc, {quantities, id}) => {
        quantities.forEach((quantity) => acc[quantity].push(id));
        return acc;
      },
      makeQuantityToIdsMap()
    );

export const makeMeasurementMetersUriParameters = (
  {
    reportDateRange,
    legendItems,
    resolution,
    displayMode,
  }: MeasurementParameters,
  endpoint: EndPoints,
  labelFactory: (labelItem: LabelItem) => string,
): EncodedUriParameters[] => {
  const quantityToIds = groupLegendItemsByQuantity(legendItems);

  return Object.keys(quantityToIds)
    .filter((quantity: Quantity) => quantityToIds[quantity].length > 0)
    .map((quantity: Quantity) => {
      const {type} = find(legendItems, {id: quantityToIds[quantity][0]})!;
      return measurementMeterUri(
        quantity,
        resolution,
        reportDateRange,
        quantityToIds[quantity],
        labelFactory({type, quantity}),
        displayMode || quantityAttributes[quantity].displayMode,
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
        const {period, customDateRange} = it.reportDateRange;
        const dateRange = Maybe.maybe<DateRange>(customDateRange)
          .map(makeCompareCustomDateRange)
          .orElseGet(() => makeCompareDateRange(period));
        return metersByQuantityRequests({
          ...it,
          reportDateRange: {period: Period.custom, customDateRange: dateRange}
        });
      }
    ).orElse([]);

const averageForUserSelectionsRequests = (
  {
    reportDateRange,
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
          ...requestParametersFrom({...it.selectionParameters, reportDateRange}),
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

const shouldFetchMeasurementsSelectionReport: FetchIfNeeded = (getState: GetState): boolean => {
  const {isFetching, isSuccessfullyFetched, error}: MeasurementState = getState().selectionMeasurement;
  return !isSuccessfullyFetched && !isFetching && error.isNothing();
};

const shouldFetchMeasurementsReport: FetchIfNeeded = (getState: GetState): boolean => {
  const {isFetching, isSuccessfullyFetched, error}: MeasurementState = getState().measurement;
  return !isSuccessfullyFetched && !isFetching && error.isNothing();
};

const shouldFetchMeasurementsMeterDetails: FetchIfNeeded = (getState: GetState): boolean => {
  const {isFetching, isSuccessfullyFetched, error}: MeasurementState = getState().domainModels.meterDetailMeasurement;
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
        dispatch(request());
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

export const fetchMeasurementsForSelectionReport = (measurementParameters: MeasurementParameters) =>
  fetchMeasurements(
    measurementParameters,
    {
      failure: measurementFailure(ReportSector.selectionReport),
      request: measurementRequest(ReportSector.selectionReport),
      success: measurementSuccess(ReportSector.selectionReport)
    },
    shouldFetchMeasurementsSelectionReport
  );

export const fetchMeasurementsForReport = (measurementParameters: MeasurementParameters) =>
  fetchMeasurements(
    measurementParameters,
    {
      failure: measurementFailure(ReportSector.report),
      request: measurementRequest(ReportSector.report),
      success: measurementSuccess(ReportSector.report)
    },
    shouldFetchMeasurementsReport
  );

export const exportReportToExcel = () =>
  (dispatch, getState: GetState) => {
    if (!getState().measurement.isExportingToExcel) {
      dispatch(exportToExcelAction(ReportSector.report)());
    }
  };

export const exportSelectionReportToExcel = () =>
  (dispatch, getState: GetState) => {
    if (!getState().selectionMeasurement.isExportingToExcel) {
      dispatch(exportToExcelAction(ReportSector.selectionReport)());
    }
  };

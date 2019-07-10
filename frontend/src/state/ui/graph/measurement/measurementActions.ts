import {find, flatMap, groupBy, map, sortBy} from 'lodash';
import {createAction, createStandardAction} from 'typesafe-actions';
import {EmptyAction, PayloadAction} from 'typesafe-actions/dist/type-helpers';
import {DateRange, Period, TemporalResolution} from '../../../../components/dates/dateModels';
import {InvalidToken} from '../../../../exceptions/InvalidToken';
import {isDefined} from '../../../../helpers/commonHelpers';
import {
  makeCompareCustomDateRange,
  makeCompareDateRange,
  queryParametersOfDateRange
} from '../../../../helpers/dateHelpers';
import {Maybe} from '../../../../helpers/Maybe';
import {
  encodeRequestParameters,
  makeUrl,
  RequestParameter,
  requestParametersFrom
} from '../../../../helpers/urlFactory';
import {GetState} from '../../../../reducers/rootReducer';
import {EndPoints} from '../../../../services/endPoints';
import {isTimeoutError, restClient, wasRequestCanceled} from '../../../../services/restClient';
import {ErrorResponse, uuid} from '../../../../types/Types';
import {logout} from '../../../../usecases/auth/authActions';
import {
  meterDetailMeasurementFailure,
  meterDetailMeasurementRequest,
  meterDetailMeasurementSuccess
} from '../../../../usecases/meter/measurements/meterDetailMeasurementActions';
import {FetchIfNeeded, noInternetConnection, requestTimeout, responseMessageOrFallback} from '../../../api/apiActions';
import {getDomainModelById} from '../../../domain-models/domainModelsSelectors';
import {getQuantity} from '../../../report/reportActions';
import {
  isAggregate,
  isKnownMedium,
  isMedium,
  LegendItem,
  LegendType,
  LegendTyped,
  ReportSector
} from '../../../report/reportModels';
import {SelectionInterval, UserSelection} from '../../../user-selection/userSelectionModels';
import {ToolbarView} from '../../toolbar/toolbarModels';
import {
  allQuantitiesMap,
  availableQuantities,
  getGroupHeaderTitle,
  MeasurementParameters,
  MeasurementRequestModel,
  MeasurementResponse,
  MeasurementResponsePart,
  MeasurementsApiResponse,
  Medium,
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

const makeMeasurementRequestModel = (
  dateRange: SelectionInterval,
  logicalMeterId: uuid[],
  quantity: string[],
  resolution: TemporalResolution,
  label?: string,
): MeasurementRequestModel => {
  const {reportAfter, reportBefore} = queryParametersOfDateRange(
    dateRange,
    RequestParameter.reportAfter,
    RequestParameter.reportBefore
  );
  return {
    label,
    logicalMeterId,
    quantity,
    reportAfter: reportAfter as string,
    reportBefore: reportBefore as string,
    resolution,
  };
};

interface LabelItem extends LegendTyped {
  quantity: Quantity;
}

interface GraphDataResponse {
  data: MeasurementsApiResponse;
}

type GraphDataRequests = Array<Promise<GraphDataResponse>>;

type MediumIdsMap = { [m in Medium]: uuid[] };
type QuantityIdsMap = { [q in Quantity]: uuid[] };

const makeMediumToIdsMap = (): MediumIdsMap =>
  Object.keys(Medium).map(key => Medium[key])
    .reduce((prev, curr) => ({...prev, [curr]: []}), {});

export const mapMediumToIds = (items: LegendItem[]): MediumIdsMap =>
  items.filter(isMedium)
    .reduce(
      (acc, {type, id}) => {
        acc[type].push(id);
        return acc;
      },
      makeMediumToIdsMap()
    );

const makeQuantityToIdsMap = (): QuantityIdsMap =>
  Object.keys(Quantity).map(k => Quantity[k])
    .reduce((acc, quantity) => ({...acc, [quantity]: []}), {});

export const mapQuantityToIds = (items: LegendItem[]): QuantityIdsMap =>
  items.filter(isMedium)
    .reduce(
      (acc, {quantities, id}) => {
        quantities.forEach(quantity => acc[quantity].push(id));
        return acc;
      },
      makeQuantityToIdsMap(),
    );

export const searchableQuantitiesFrom = (type: LegendType): Quantity[] =>
  allQuantitiesMap[type].filter(availableQuantities);

export const makeQuantityParamFrom = (quantity: Quantity, displayMode?: QuantityDisplayMode): string =>
  `${quantity}::${displayMode || quantityAttributes[quantity].displayMode}`;

export const undefinedLabelFactory = _ => undefined;

export const mediumLabelFactory = ({type}: LabelItem): string => `${getGroupHeaderTitle(type)}`;

const measurementsRequestModelsByMedium = (
  {
    displayMode,
    reportDateRange,
    legendItems,
    resolution,
  }: MeasurementParameters,
  labelFactory: (labelItem: LabelItem) => string | undefined = undefinedLabelFactory,
): MeasurementRequestModel[] => {
  const mediumToIds = mapMediumToIds(legendItems);

  return Object.keys(mediumToIds)
    .filter((medium: Medium) => mediumToIds[medium].length > 0)
    .filter(isKnownMedium)
    .map(medium => {
      const quantityParams = searchableQuantitiesFrom(medium).map(it => makeQuantityParamFrom(it, displayMode));
      return makeMeasurementRequestModel(
        reportDateRange,
        mediumToIds[medium],
        quantityParams,
        resolution,
        labelFactory({type: medium, quantity: getQuantity({type: medium})})
      );
    });
};

const measurementsRequestModelsByQuantity = (
  {
    reportDateRange,
    legendItems,
    resolution,
  }: MeasurementParameters,
  labelFactory: (labelItem: LabelItem) => string | undefined = undefinedLabelFactory,
): MeasurementRequestModel[] => {
  const quantityToIds = mapQuantityToIds(legendItems);

  return Object.keys(quantityToIds)
    .filter((quantity: Quantity) => quantityToIds[quantity].length > 0)
    .map((quantity: Quantity) => {
      const {type} = find(legendItems, {id: quantityToIds[quantity][0]})!;
      return makeMeasurementRequestModel(
        reportDateRange,
        quantityToIds[quantity],
        [makeQuantityParamFrom(quantity)],
        resolution,
        labelFactory({type, quantity})
      );
    });
};

export const measurementsRequestModelsOf = (
  parameters: MeasurementParameters,
  labelFactory: (labelItem: LabelItem) => string | undefined = undefinedLabelFactory,
): MeasurementRequestModel[] =>
  parameters.view === ToolbarView.table
    ? measurementsRequestModelsByMedium(parameters, labelFactory)
    : measurementsRequestModelsByQuantity(parameters, labelFactory);

export const requestModelsByType = (parameters: MeasurementParameters): MeasurementRequestModel[] => {
  const byType = groupBy(parameters.legendItems, it => it.type);
  const measurementParameters: MeasurementParameters[] = flatMap(
    Object.keys(byType)
      .map(type => byType[type])
      .map(legendItems => ({...parameters, legendItems})));

  return flatMap(measurementParameters.map(p => measurementsRequestModelsOf(p, mediumLabelFactory)));
};

export const metersMeasurementsRequests = (parameters: MeasurementParameters): GraphDataRequests =>
  measurementsRequestModelsOf(parameters)
    .map(requestModel => restClient.post(EndPoints.measurements, requestModel));

const averageForSelectedMetersRequests = (parameters: MeasurementParameters): GraphDataRequests => {
  if (parameters.shouldShowAverage) {
    return requestModelsByType(parameters)
      .map(requestModel => restClient.post(EndPoints.measurementsAverage, requestModel));
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
        return metersMeasurementsRequests({
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
  const {isExportingToExcel, isFetching, isSuccessfullyFetched, error} = getState().selectionMeasurement;
  return isExportingToExcel || !isSuccessfullyFetched && !isFetching && error.isNothing();
};

const shouldFetchMeasurementsReport: FetchIfNeeded = (getState: GetState): boolean => {
  const {isFetching, isSuccessfullyFetched, error} = getState().measurement;
  return !isSuccessfullyFetched && !isFetching && error.isNothing();
};

const shouldFetchMeasurementsMeterDetails: FetchIfNeeded = (getState: GetState): boolean => {
  const {isFetching, isSuccessfullyFetched, error} = getState().domainModels.meterDetailMeasurement;
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
      const meters: GraphDataRequests = metersMeasurementsRequests(parameters);
      const meterAverage: GraphDataRequests = averageForSelectedMetersRequests(parameters);
      const userSelectionAverage: GraphDataRequests = averageForUserSelectionsRequests(parameters, getState);
      const compare: GraphDataRequests = compareMeterRequests(parameters);

      if (meters.length || meterAverage.length || userSelectionAverage.length || compare.length) {
        dispatch(request());
        try {
          const [meterResponses, meterAverageResponses, averageResponses, compareResponses]: GraphDataResponse[][] =
            await Promise.all([
              Promise.all(meters),
              Promise.all(meterAverage),
              Promise.all(userSelectionAverage),
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

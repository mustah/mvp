import {flatMap, map} from 'lodash';
import {TemporalResolution} from '../../../../components/dates/dateModels';
import {InvalidToken} from '../../../../exceptions/InvalidToken';
import {isDefined} from '../../../../helpers/commonUtils';
import {cityWithoutCountry} from '../../../../helpers/formatters';
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
  Dispatcher,
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
import {
  SelectionTreeCity,
  SelectionTreeEntities,
  SelectionTreeMeter,
} from '../../../selection-tree/selectionTreeModels';
import {SelectedParameters, SelectionInterval} from '../../../user-selection/userSelectionModels';
import {
  allQuantities,
  initialMeterMeasurementsState,
  Measurement,
  MeasurementApiResponse,
  MeasurementResponsePart,
  MeasurementResponses,
  MeasurementState,
  Medium,
  MeterMeasurementsState,
  Quantity
} from './measurementModels';
import {measurementDataFormatter} from './measurementSchema';

export const MEASUREMENT_REQUEST = 'MEASUREMENT_REQUEST';
export const measurementRequest = emptyActionOf(MEASUREMENT_REQUEST);

export const MEASUREMENT_SUCCESS = 'MEASUREMENT_SUCCESS';
export const measurementSuccess = payloadActionOf<MeasurementResponses>(MEASUREMENT_SUCCESS);

export const MEASUREMENT_FAILURE = 'MEASUREMENT_FAILURE';
export const measurementFailure = payloadActionOf<Maybe<ErrorResponse>>(MEASUREMENT_FAILURE);

export const MEASUREMENT_CLEAR_ERROR = 'MEASUREMENT_CLEAR_ERROR';
export const measurementClearError = emptyActionOf(MEASUREMENT_CLEAR_ERROR);

export const EXPORT_TO_EXCEL = 'EXPORT_TO_EXCEL';
const exportToExcelAction = emptyActionOf(EXPORT_TO_EXCEL);
export const exportToExcel =
  () =>
    (dispatch, getState: GetState) => {
      if (!getState().measurement.isExportingToExcel) {
        dispatch(exportToExcelAction());
      }
    };
export const EXPORT_TO_EXCEL_SUCCESS = 'EXPORT_TO_EXCEL_SUCCESS';
export const exportToExcelSuccess = emptyActionOf(EXPORT_TO_EXCEL_SUCCESS);

const measurementMeterUri = (
  quantity: Quantity,
  resolution: TemporalResolution,
  meters: uuid[],
  {dateRange}: SelectedParameters,
): EncodedUriParameters =>
  encodeRequestParameters({
    ...requestParametersFrom({dateRange}),
    quantity,
    resolution,
    logicalMeterId: meters.map((id: uuid): string => id.toString()),
  });

const measurementCityUri = (
  quantity: Quantity,
  resolution: TemporalResolution,
  city: uuid,
  selectedParameters: SelectedParameters,
): EncodedUriParameters =>
  encodeRequestParameters({
    ...requestParametersFrom(selectedParameters),
    quantity,
    resolution,
    city: city.toString(),
    label: cityWithoutCountry(city.toString()),
  });

interface GraphDataResponse {
  data: MeasurementApiResponse;
}

export const isSelectedMeter = (listItem: uuid): boolean =>
  (listItem.toString().match(/[,:]/) || []).length === 0;

export const isSelectedCity = (listItem: uuid): boolean =>
  (listItem.toString().match(/[,]/g) || []).length === 1 &&
  (listItem.toString().match(/[:]/) || []).length === 0;

type GraphDataRequests = Array<Promise<GraphDataResponse>>;

interface GroupedRequests {
  average: GraphDataRequests;
  cities: GraphDataRequests;
  meters: GraphDataRequests;
}

const requestsPerQuantity = ({
  quantities,
  resolution,
  selectedListItems,
  selectionParameters,
  selectionTreeEntities,
  shouldMakeAverageRequest,
}: MeasurementParameters): GroupedRequests => {
  const meterByQuantity: Partial<{ [quantity in Quantity]: Set<uuid> }> = {};
  quantities.forEach((quantity: Quantity) => {
    meterByQuantity[quantity] = new Set();
  });
  const quantitiesByCity: {[key: string]: Set<Quantity>} = {};

  const requests: GroupedRequests = {
    average: [],
    meters: [],
    cities: [],
  };

  selectedListItems
    .filter(isSelectedCity)
    .map((cityId: uuid) => selectionTreeEntities.cities[cityId])
    .filter(isDefined)
    .forEach(({medium, id}: SelectionTreeCity) => {
      medium.forEach((singleMedium: Medium) => {
        const quantitiesInCity: Quantity[] = allQuantities[singleMedium];
        quantities
          .filter((quantity: Quantity) => quantitiesInCity.includes(quantity))
          .forEach((quantity: Quantity) => quantitiesByCity[id]
            ? quantitiesByCity[id].add(quantity)
            : quantitiesByCity[id] = new Set<Quantity>([quantity]));
      });
    });

  selectedListItems
    .filter(isSelectedMeter)
    .map((meterId: uuid) => selectionTreeEntities.meters[meterId])
    .filter(isDefined)
    .forEach(({medium, id}: SelectionTreeMeter) => {
      const meterQuantities: Quantity[] = allQuantities[medium];
      quantities
        .filter((quantity: Quantity) => meterQuantities.includes(quantity))
        .forEach((quantity: Quantity) => meterByQuantity[quantity]!.add(id));
    });

  requests.cities = Object.keys(quantitiesByCity)
    .reduce(
      (accumulator: GraphDataRequests, city: uuid) => [
        ...accumulator,
        ...Array.from(quantitiesByCity[city]).map((quantity: Quantity) =>
          restClient.getParallel(makeUrl(
            EndPoints.measurements.concat('/average'),
            measurementCityUri(quantity, resolution, city, selectionParameters),
          ))
        )
      ],
      []
    );

  requests.meters = Object.keys(meterByQuantity)
    .filter((quantity) => meterByQuantity[quantity]!.size)
    .map((quantity: Quantity) =>
      restClient.getParallel(makeUrl(
        EndPoints.measurements,
        measurementMeterUri(quantity, resolution, Array.from(meterByQuantity[quantity]!), selectionParameters),
      )));

  if (shouldMakeAverageRequest) {
    requests.average = Object.keys(meterByQuantity)
      .filter((quantity) => meterByQuantity[quantity]!.size > 1)
      .map((quantity: Quantity) =>
        restClient.getParallel(makeUrl(
          EndPoints.measurements.concat('/average'),
          measurementMeterUri(quantity, resolution, Array.from(meterByQuantity[quantity]!), selectionParameters),
        )));
  }

  return requests;
};

const removeUndefinedValues = (averageEntity: MeasurementResponsePart): MeasurementResponsePart => ({
  ...averageEntity,
  values: averageEntity.values.filter(({value}) => value !== undefined),
});

export interface MeasurementParameters {
  quantities: Quantity[];
  resolution: TemporalResolution;
  selectionTreeEntities: SelectionTreeEntities;
  selectedListItems: uuid[];
  selectionParameters: SelectedParameters;
  shouldMakeAverageRequest?: boolean;
}

const fetchMeasurementsAsync = async (parameters: MeasurementParameters, dispatch: Dispatcher): Promise<void> => {
  const {average, cities, meters}: GroupedRequests = requestsPerQuantity(parameters);

  if (parameters.quantities.length && (cities.length + meters.length)) {
    dispatch(measurementRequest());
    try {
      const [meterResponses, averageResponses, citiesResponses]: GraphDataResponse[][] =
        await Promise.all([Promise.all(meters), Promise.all(average), Promise.all(cities)]);

      const measurementResponse: MeasurementResponses = {
        measurements: flatMap(meterResponses, 'data'),
        average: map(
          flatMap(averageResponses, 'data'),
          removeUndefinedValues,
        ),
        cities: flatMap(citiesResponses, 'data'),
      };

      dispatch(measurementSuccess(measurementResponse));
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
};

const shouldFetchMeasurements: FetchIfNeeded = (getState: GetState): boolean => {
  const {isFetching, isSuccessfullyFetched, error}: MeasurementState = getState().measurement;
  return !isSuccessfullyFetched && !isFetching && error.isNothing();
};

export const fetchMeasurements = (requestParameters: MeasurementParameters) =>
  async (dispatch, getState: GetState) => {
    if (shouldFetchMeasurements(getState)) {
      await fetchMeasurementsAsync(requestParameters, dispatch);
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

import {flatMap, map} from 'lodash';
import {DateRange, Period} from '../../../../components/dates/dateModels';
import {InvalidToken} from '../../../../exceptions/InvalidToken';
import {isDefined} from '../../../../helpers/commonUtils';
import {toPeriodApiParameters} from '../../../../helpers/dateHelpers';
import {Maybe} from '../../../../helpers/Maybe';
import {makeUrl} from '../../../../helpers/urlFactory';
import {EndPoints} from '../../../../services/endPoints';
import {isTimeoutError, restClient, wasRequestCanceled} from '../../../../services/restClient';
import {EncodedUriParameters, uuid} from '../../../../types/Types';
import {OnLogout} from '../../../../usecases/auth/authModels';
import {ReportContainerState} from '../../../../usecases/report/containers/ReportContainer';
import {noInternetConnection, requestTimeout, responseMessageOrFallback} from '../../../api/apiActions';
import {NormalizedPaginated} from '../../../domain-models-paginated/paginatedDomainModels';
import {MeterDetails} from '../../../domain-models/meter-details/meterDetailsModels';
import {
  SelectionTreeCity,
  SelectionTreeEntities,
  SelectionTreeMeter,
} from '../../../selection-tree/selectionTreeModels';
import {
  allQuantities,
  initialMeterMeasurementsState,
  initialState,
  Measurement,
  MeasurementApiResponse,
  MeasurementResponsePart,
  MeasurementResponses,
  Medium,
  MeterMeasurementsState,
  Quantity
} from './measurementModels';
import {measurementDataFormatter} from './measurementSchema';

const measurementMeterUri = (
  quantity: Quantity,
  meters: uuid[],
  timePeriod: Period,
  customDateRange: Maybe<DateRange>,
): string =>
  `quantities=${quantity}` +
  `&meters=${meters.join(',')}` +
  `&${toPeriodApiParameters({period: timePeriod, customDateRange}).join('&')}`;

const measurementCityUri = (
  quantity: Quantity,
  cities: uuid[],
  timePeriod: Period,
  customDateRange: Maybe<DateRange>,
): string =>
  `quantities=${quantity}` +
  `&${cities.map((city) => `city=${city}`).join('&')}` +
  `&${toPeriodApiParameters({period: timePeriod, customDateRange}).join('&')}`;

interface GraphDataResponse {
  data: MeasurementApiResponse;
}

export const isSelectedMeter = (listItem: uuid): boolean =>
  (listItem.toString().match(/[,:]/) || []).length === 0;

export const isSelectedCity = (listItem: uuid): boolean =>
  (listItem.toString().match(/[,]/g) || []).length === 1 &&
  (listItem.toString().match(/[:]/) || []).length === 0;

type OnUpdateGraph = (state: ReportContainerState) => void;

export interface MeasurementOptions {
  selectionTreeEntities: SelectionTreeEntities;
  selectedIndicators: Medium[];
  quantities: Quantity[];
  selectedListItems: uuid[];
  timePeriod: Period;
  customDateRange: Maybe<DateRange>;
  updateState: OnUpdateGraph;
  logout: OnLogout;
}

interface GroupedRequests {
  average: Array<Promise<GraphDataResponse>>;
  meters: Array<Promise<GraphDataResponse>>;
  cities: Array<Promise<GraphDataResponse>>;
}

const requestsPerQuantity = (
  quantities: Quantity[],
  selectionTreeEntities: SelectionTreeEntities,
  selectedListItems: uuid[],
  timePeriod: Period,
  customDateRange: Maybe<DateRange>,
): GroupedRequests => {

  const meterByQuantity: Partial<{[quantity in Quantity]: Set<uuid>}> = {};
  const cityByQuantity: Partial<{[quantity in Quantity]: Set<uuid>}> = {};
  quantities.forEach((quantity: Quantity) => {
    meterByQuantity[quantity] = new Set();
    cityByQuantity[quantity] = new Set();
  });

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
        const cityQuantities: Quantity[] = allQuantities[singleMedium];
        quantities
          .filter((quantity: Quantity) => cityQuantities.includes(quantity))
          .forEach((quantity: Quantity) => cityByQuantity[quantity]!.add(id));
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

  requests.cities = Object.keys(cityByQuantity)
    .filter((quantity: Quantity) => cityByQuantity[quantity]!.size)
    .map((quantity: Quantity) =>
      restClient.getParallel(makeUrl(
        EndPoints.measurements.concat('/cities'),
        measurementCityUri(quantity, Array.from(cityByQuantity[quantity]!), timePeriod, customDateRange),
      )));

  requests.meters = Object.keys(meterByQuantity)
    .filter((quantity) => meterByQuantity[quantity]!.size)
    .map((quantity: Quantity) =>
      restClient.getParallel(makeUrl(
        EndPoints.measurements,
        measurementMeterUri(quantity, Array.from(meterByQuantity[quantity]!), timePeriod, customDateRange),
      )));

  requests.average = Object.keys(meterByQuantity)
    .filter((quantity) => meterByQuantity[quantity]!.size > 1)
    .map((quantity: Quantity) =>
      restClient.getParallel(makeUrl(
        EndPoints.measurements.concat('/average'),
        measurementMeterUri(quantity, Array.from(meterByQuantity[quantity]!), timePeriod, customDateRange),
      )));

  return requests;
};

const removeUndefinedValues = (averageEntity: MeasurementResponsePart): MeasurementResponsePart => ({
  ...averageEntity,
  values: averageEntity.values.filter(({value}) => value !== undefined),
});

export const fetchMeasurements =
  async ({
    selectionTreeEntities,
    selectedIndicators,
    quantities,
    selectedListItems,
    timePeriod,
    customDateRange,
    updateState,
    logout,
  }: MeasurementOptions): Promise<void> => {
    const {average, cities, meters}: GroupedRequests = requestsPerQuantity(
      quantities,
      selectionTreeEntities,
      selectedListItems,
      timePeriod,
      customDateRange,
    );

    if (
      selectedIndicators.length === 0 ||
      (cities.length + meters.length) === 0 ||
      quantities.length === 0
    ) {
      updateState({...initialState});
      return;
    }

    try {
      const [meterResponses, averageResponses, citiesResponses]: GraphDataResponse[][] =
        await Promise.all([Promise.all(meters), Promise.all(average), Promise.all(cities)]);
      const graphData: MeasurementResponses = {
        measurements: flatMap(meterResponses, 'data'),
        average: map(
          flatMap(averageResponses, 'data'),
          removeUndefinedValues,
        ),
        cities: flatMap(citiesResponses, 'data'),
      };

      updateState({
        ...initialState,
        measurementResponse: graphData,
      });
    } catch (error) {
      if (error instanceof InvalidToken) {
        await logout(error);
      } else if (wasRequestCanceled(error)) {
        return;
      } else if (isTimeoutError(error)) {
        updateState({...initialState, error: Maybe.maybe(requestTimeout())});
      } else if (!error.response) {
        updateState({...initialState, error: Maybe.maybe(noInternetConnection())});
      } else {
        updateState({
          ...initialState,
          error: Maybe.maybe(responseMessageOrFallback(error.response)),
        });
      }
    }
  };

export type OnUpdate = (state: MeterMeasurementsState) => void;

interface MeasurementPagedApiResponse {
  data: NormalizedPaginated<Measurement>;
}

export const fetchMeasurementsPaged =
  async (
    meter: MeterDetails,
    updateState: OnUpdate,
    logout: OnLogout,
  ): Promise<void> => {
    try {
      const measurementUrl: EncodedUriParameters = makeUrl(
        EndPoints.measurementsPaged,
        `sort=created,desc&sort=quantity,asc&logicalMeterId=${meter.id}&size=${(50 * meter.measurements.length)}`,
      );
      const {data}: MeasurementPagedApiResponse = await restClient.get(measurementUrl);

      updateState({
        ...initialMeterMeasurementsState,
        measurementPages: measurementDataFormatter(data),
      });
    } catch (error) {
      if (error instanceof InvalidToken) {
        await logout(error);
      } else if (wasRequestCanceled(error)) {
        return;
      } else if (isTimeoutError(error)) {
        updateState({...initialMeterMeasurementsState, error: Maybe.maybe(requestTimeout())});
      } else if (!error.response) {
        updateState({...initialMeterMeasurementsState, error: Maybe.maybe(noInternetConnection())});
      } else {
        updateState({
          ...initialMeterMeasurementsState,
          error: Maybe.maybe(responseMessageOrFallback(error.response)),
        });
      }
    }
  };

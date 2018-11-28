import {flatMap, map} from 'lodash';
import {InvalidToken} from '../../../../exceptions/InvalidToken';
import {isDefined} from '../../../../helpers/commonUtils';
import {cityWithoutCountry} from '../../../../helpers/formatters';
import {Maybe} from '../../../../helpers/Maybe';
import {encodeRequestParameters, makeUrl, requestParametersFrom} from '../../../../helpers/urlFactory';
import {EndPoints} from '../../../../services/endPoints';
import {isTimeoutError, restClient, wasRequestCanceled} from '../../../../services/restClient';
import {EncodedUriParameters, uuid} from '../../../../types/Types';
import {OnLogout} from '../../../../usecases/auth/authModels';
import {ReportContainerState} from '../../../../usecases/report/containers/ReportContainer';
import {noInternetConnection, requestTimeout, responseMessageOrFallback} from '../../../api/apiActions';
import {NormalizedPaginated} from '../../../domain-models-paginated/paginatedDomainModels';
import {
  SelectionTreeCity,
  SelectionTreeEntities,
  SelectionTreeMeter,
} from '../../../selection-tree/selectionTreeModels';
import {SelectedParameters} from '../../../user-selection/userSelectionModels';
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
  {dateRange}: SelectedParameters,
): EncodedUriParameters =>
  encodeRequestParameters({
    ...requestParametersFrom({dateRange}),
    quantity,
    logicalMeterId: meters.map((id: uuid): string => id.toString()),
  });

const measurementCityUri = (
  quantity: Quantity,
  city: uuid,
  selectedParameters: SelectedParameters,
): EncodedUriParameters =>
  encodeRequestParameters({
    ...requestParametersFrom(selectedParameters),
    quantity,
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

type OnUpdateGraph = (state: ReportContainerState) => void;

type GraphDataRequests = Array<Promise<GraphDataResponse>>;

interface GroupedRequests {
  average: GraphDataRequests;
  meters: GraphDataRequests;
  cities: GraphDataRequests;
}

const requestsPerQuantity = (
  quantities: Quantity[],
  selectionTreeEntities: SelectionTreeEntities,
  selectedListItems: uuid[],
  selectionParameters: SelectedParameters,
): GroupedRequests => {
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
            measurementCityUri(quantity, city, selectionParameters),
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
        measurementMeterUri(quantity, Array.from(meterByQuantity[quantity]!), selectionParameters),
      )));

  requests.average = Object.keys(meterByQuantity)
    .filter((quantity) => meterByQuantity[quantity]!.size > 1)
    .map((quantity: Quantity) =>
      restClient.getParallel(makeUrl(
        EndPoints.measurements.concat('/average'),
        measurementMeterUri(quantity, Array.from(meterByQuantity[quantity]!), selectionParameters),
      )));

  return requests;
};

const removeUndefinedValues = (averageEntity: MeasurementResponsePart): MeasurementResponsePart => ({
  ...averageEntity,
  values: averageEntity.values.filter(({value}) => value !== undefined),
});

export interface MeasurementOptions {
  selectionTreeEntities: SelectionTreeEntities;
  selectedIndicators: Medium[];
  quantities: Quantity[];
  selectedListItems: uuid[];
  updateState: OnUpdateGraph;
  logout: OnLogout;
  selectionParameters: SelectedParameters;
}

export const fetchMeasurements =
  async ({
    selectionTreeEntities,
    selectedIndicators,
    quantities,
    selectedListItems,
    updateState,
    logout,
    selectionParameters,
  }: MeasurementOptions): Promise<void> => {
    const {average, cities, meters}: GroupedRequests = requestsPerQuantity(
      quantities,
      selectionTreeEntities,
      selectedListItems,
      selectionParameters,
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

export const fetchMeasurementsPaged = async (id: uuid, updateState: OnUpdate, logout: OnLogout): Promise<void> => {
  try {
    const measurementUrl: EncodedUriParameters = makeUrl(
      EndPoints.measurementsPaged,
      `sort=created,desc&sort=quantity,asc&logicalMeterId=${id}&size=300`,
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

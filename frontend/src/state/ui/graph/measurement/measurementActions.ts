import {DateRange, Period} from '../../../../components/dates/dateModels';
import {Medium} from '../../../../components/indicators/indicatorWidgetModels';
import {InvalidToken} from '../../../../exceptions/InvalidToken';
import {now, toPeriodApiParameters} from '../../../../helpers/dateHelpers';
import {Maybe} from '../../../../helpers/Maybe';
import {makeUrl} from '../../../../helpers/urlFactory';
import {EndPoints} from '../../../../services/endPoints';
import {isTimeoutError, restClient, wasRequestCanceled} from '../../../../services/restClient';
import {EncodedUriParameters, uuid} from '../../../../types/Types';
import {OnLogout} from '../../../../usecases/auth/authModels';
import {OnUpdateGraph} from '../../../../usecases/report/containers/ReportContainer';
import {noInternetConnection, requestTimeout, responseMessageOrFallback} from '../../../api/apiActions';
import {initialState, MeasurementApiResponse, MeasurementResponses, Quantity} from './measurementModels';

const measurementMeterUri = (
  quantities: Quantity[],
  meters: uuid[],
  timePeriod: Period,
  customDateRange: Maybe<DateRange>,
): string =>
  `quantities=${quantities.join(',')}` +
  `&meters=${meters.join(',')}` +
  `&${toPeriodApiParameters({now: now(), period: timePeriod, customDateRange}).join('&')}`;

const measurementCityUri = (
  quantities: Quantity[],
  cities: uuid[],
  timePeriod: Period,
  customDateRange: Maybe<DateRange>,
): string =>
  `quantities=${quantities.join(',')}` +
  `&${cities.map((city) => `city=${city}`).join('&')}` +
  `&${toPeriodApiParameters({now: now(), period: timePeriod, customDateRange}).join('&')}`;

interface GraphDataResponse {
  data: MeasurementApiResponse;
}

export const isSelectedMeter = (listItem: uuid): boolean =>
  (listItem.toString().match(/[,:]/) || []).length === 0;

export const isSelectedCity = (listItem: uuid): boolean =>
  (listItem.toString().match(/[,]/g) || []).length === 1 &&
  (listItem.toString().match(/[:]/) || []).length === 0;

interface MeasurementOptions {
  selectedIndicators: Medium[];
  quantities: Quantity[];
  selectedListItems: uuid[];
  timePeriod: Period;
  customDateRange: Maybe<DateRange>;
  updateState: OnUpdateGraph;
  logout: OnLogout;
}

const noopRequest = new Promise<GraphDataResponse>((resolve) => resolve({data: []}));

export const fetchMeasurements =
  async ({
    selectedIndicators,
    quantities,
    selectedListItems,
    timePeriod,
    customDateRange,
    updateState,
    logout,
  }: MeasurementOptions): Promise<void> => {

    const selectedMeters = selectedListItems.filter(isSelectedMeter);
    const selectedCities = selectedListItems.filter(isSelectedCity);

    if (
      selectedIndicators.length === 0 ||
      (selectedMeters.length + selectedCities.length) === 0 ||
      quantities.length === 0
    ) {
      updateState({...initialState});
      return;
    }

    const averageUrl: EncodedUriParameters = makeUrl(
      EndPoints.measurements.concat('/average'),
      measurementMeterUri(quantities, selectedMeters, timePeriod, customDateRange),
    );

    const averageRequest: () => Promise<GraphDataResponse> =
      selectedMeters.length > 1
        ? () => restClient.get(averageUrl)
        : () => noopRequest;

    const cityUrl: EncodedUriParameters = makeUrl(
      EndPoints.measurements.concat('/cities'),
      measurementCityUri(quantities, selectedCities, timePeriod, customDateRange),
    );

    const cityRequest: () => Promise<GraphDataResponse> =
      selectedCities.length
        ? () => restClient.get(cityUrl)
        : () => noopRequest;

    const measurementUrl: EncodedUriParameters = makeUrl(
      EndPoints.measurements,
      measurementMeterUri(quantities, selectedMeters, timePeriod, customDateRange),
    );

    const measurementRequest: () => Promise<GraphDataResponse> =
      selectedMeters.length
        ? () => restClient.get(measurementUrl)
        : () => noopRequest;

    try {
      const response: [GraphDataResponse, GraphDataResponse, GraphDataResponse] =
        await Promise.all([measurementRequest(), averageRequest(), cityRequest()]);

      const graphData: MeasurementResponses = {
        measurement: response[0].data,
        average: response[1].data.map((averageEntity) => ({
          ...averageEntity,
          values: averageEntity.values.filter(({value}) => value !== undefined),
        })),
        cities: response[2].data,
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

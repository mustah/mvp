import {SelectionItem} from '../state/domain-models/domainModels';
import {Pagination} from '../state/ui/pagination/paginationModels';
import {SelectedParameters, SelectionInterval} from '../state/user-selection/userSelectionModels';
import {EncodedUriParameters, Omit} from '../types/Types';
import {toPeriodApiParameters} from './dateHelpers';
import {Maybe} from './Maybe';

interface ParameterNames {
  [key: string]: string;
}

const baseParameterNames: ParameterNames = {
  addresses: 'address',
  alarms: 'alarm',
  cities: 'city',
  manufacturers: 'manufacturer',
  media: 'medium',
  productModels: 'productModel',
  gatewaySerials: 'gatewaySerial',
  secondaryAddresses: 'secondaryAddress',
  facilities: 'facility',
};

const gatewayParameterNames: ParameterNames = {
  ...baseParameterNames,
  gatewayStatuses: 'status',
  meterStatuses: 'meterStatus',
};

const meterParameterNames: ParameterNames = {
  ...baseParameterNames,
  meterIds: 'id',
  meterStatuses: 'status',
  gatewayStatuses: 'gatewayStatus',
};

export const encodedUriParametersFrom = (
  uriParams: EncodedUriParameters[],
): EncodedUriParameters => uriParams.length ? uriParams.join('&') : '';

export const toPaginationApiParameters = ({page, size}: Pagination) => [
  `size=${encodeURIComponent(size.toString())}`,
  `page=${encodeURIComponent(page.toString())}`,
];

export const toEntityApiParametersMeters =
  (selectionParameters: Omit<SelectedParameters, 'dateRange'>) =>
    toEntityApiParameters(selectionParameters, meterParameterNames);

export const toEntityApiParametersGateways =
  (selectionParameters: Omit<SelectedParameters, 'dateRange'>) =>
    toEntityApiParameters(selectionParameters, gatewayParameterNames);

// TODO: perhaps make sure it could handle if dateRange is included, as it is now the function
// would most likely fail.
const toEntityApiParameters = (
  selectionParameters: Omit<SelectedParameters, 'dateRange'>,
  parameterNames: ParameterNames,
): EncodedUriParameters[] =>
  Object.keys(selectionParameters)
    .reduce((prev: EncodedUriParameters[], parameter: string) =>
      [
        ...prev,
        ...selectionParameters[parameter]
          .filter(({id}: SelectionItem) => id !== undefined)
          .map(({id}: SelectionItem) => `${parameterNames[parameter]}=${encodeURIComponent(id.toString())}`),
      ], []);

export const makeApiParametersOf = (
  start: Date,
  selectionInterval: SelectionInterval,
): EncodedUriParameters => {
  return toPeriodApiParameters({
    now: start,
    period: selectionInterval.period,
    customDateRange: Maybe.maybe(selectionInterval.customDateRange),
  }).join('&');
};

export const makeUrl =
  (endpoint: string, parameters?: EncodedUriParameters): EncodedUriParameters =>
    parameters && parameters.length ? endpoint + '?' + parameters : endpoint;

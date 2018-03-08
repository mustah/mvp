import {Period} from '../components/dates/dateModels';
import {SelectedParameters} from '../state/search/selection/selectionModels';
import {Pagination} from '../state/ui/pagination/paginationModels';
import {uuid} from '../types/Types';
import {currentDateRange, toApiParameters} from './dateHelpers';

interface ParameterNames {
  [key: string]: string;
}

interface ParameterCallbacks {
  [key: string]: ((parameter: string) => string[]);
}

const parameterCallbacks: ParameterCallbacks = {
  period: (parameter: string) => toApiParameters(currentDateRange(parameter as Period)),
};

const baseParameterNames: ParameterNames = {
  cities: 'city.id',
  addresses: 'address.id',
  alarms: 'alarm',
  productModels: 'productModel',
  manufacturers: 'manufacturer',
};

const gatewayParameterNames: ParameterNames = {
  ...baseParameterNames,
  gatewayStatuses: 'status',
  meterStatuses: 'meterStatus',
};

const meterParameterNames: ParameterNames = {
  ...baseParameterNames,
  meterStatuses: 'status',
  gatewayStatuses: 'gatewayStatus',
};

export const encodedUriParametersForMeters = (
  pagination: Pagination,
  selectedIds: SelectedParameters,
): string => {
  return encodedUriParametersFrom({
    pagination,
    selectedIds,
    parameterNames: meterParameterNames,
    parameterCallbacks,
  });
};

export const encodedUriParametersForAllMeters = (selectedIds: SelectedParameters): string => {
  return encodedUriParametersFrom({
    selectedIds,
    parameterNames: meterParameterNames,
    parameterCallbacks,
  });
};

export const encodedUriParametersForGateways = (selectedIds: SelectedParameters): string => {
  return encodedUriParametersFrom({
    selectedIds,
    parameterNames: gatewayParameterNames,
    parameterCallbacks,
  });
};

interface UriParameters {
  pagination?: Pagination;
  selectedIds: SelectedParameters;
  parameterNames: ParameterNames;
  parameterCallbacks: ParameterCallbacks;
}

const encodedUriParametersFrom =
  (
    {
      pagination = {page: -1, size: -1},
      selectedIds,
      parameterNames,
      parameterCallbacks,
    }: UriParameters,
  ): string => {
    const parameters: string[] = [];

    if (pagination.page !== -1) {
      const {page, size} = pagination;
      parameters.push(`size=${encodeURIComponent(size.toString())}`);
      parameters.push(`page=${encodeURIComponent(page.toString())}`);
    }

    const addParameterWith = (name: string, value: uuid | Period) =>
      parameters.push((parameterNames[name]) + '=' + encodeURIComponent(value.toString()));

    Object.keys(selectedIds).forEach((parameter: string) => {
      const selection = selectedIds[parameter];
      if (parameterCallbacks[parameter]) {
        parameterCallbacks[parameter](selection).forEach((param: string) => parameters.push(param));
      } else if (Array.isArray(selection)) {
        selection.forEach((value: uuid) => addParameterWith(parameter, value));
      } else {
        addParameterWith(parameter, selection);
      }
    });
    return parameters.length ? parameters.join('&') : '';
  };

export const makeUrl = (endpoint: string, encodedUriParameters?: string): string => {
  if (encodedUriParameters && encodedUriParameters.length) {
    return endpoint + '?' + encodedUriParameters;
  } else {
    return endpoint;
  }
};

import {Period} from '../components/dates/dateModels';
import {EndPoints} from '../state/domain-models/domainModels';
import {PaginationMetadata} from '../state/domain-models/paginatedDomainModels';
import {SelectedParameters} from '../state/search/selection/selectionModels';
import {Pagination} from '../state/ui/pagination/paginationModels';
import {uuid} from '../types/Types';
import {currentDateRange, toApiParameters} from './dateHelpers';
import {Maybe} from './Maybe';

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
};

const gatewayParameterNames: ParameterNames = {
  ...baseParameterNames,
  gatewayStatuses: 'status.id',
  productModels: 'productModel',
  meterStatuses: 'MeterStatus.id',
  alarms: 'meterAlarm',
  manufacturers: 'meterManufacturer',
};

const meterParameterNames: ParameterNames = {
  ...baseParameterNames,
  alarms: 'alarm',
  meterStatuses: 'status.id',
  manufacturers: 'manufacturer',
  gatewayStatuses: 'gatewayStatus.id',
  productModels: 'gatewayProductModel',
};

export const encodedUriParametersForMeters = (pagination: Pagination, selectedIds: SelectedParameters): string => {
  return encodedUriParametersFrom(pagination, selectedIds, meterParameterNames, parameterCallbacks);
};

export const encodedUriParametersForGateways = (pagination, selectedIds: SelectedParameters): string => {
  return encodedUriParametersFrom(pagination, selectedIds, meterParameterNames, parameterCallbacks);
};

const encodedUriParametersFrom =
  (
    {requestedPage, size}: Pagination,
    selectedIds: SelectedParameters,
    parameterNames: ParameterNames,
    parameterCallbacks: ParameterCallbacks,
  ): string => {
    const parameters: string[] = [];
    parameters.push(`size=${encodeURIComponent(size.toString())}`);
    parameters.push(`page=${encodeURIComponent(requestedPage.toString())}`);

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

const navigatePageinatedEndpoint =
  (direction: 'next' | 'previous', endpoint: EndPoints, currentPage: PaginationMetadata): Maybe<string> => {
    if ((currentPage.last && direction === 'next') || (currentPage.first && direction === 'previous')) {
      return Maybe.nothing();
    }

    const parameters: string[] = [];
    if (Array.isArray(currentPage.sort)) {
      currentPage.sort.forEach((current) =>
        parameters.push(`sort=${encodeURIComponent(current.property)},${current.direction}`));
    }

    const nextPage = direction === 'next' ? currentPage.number + 1 : currentPage.number - 1;
    parameters.push(`number=${nextPage}`);

    return Maybe.just(makeUrl(endpoint, parameters.join('&')));
  };

export const urlForNextPage = (endpoint: EndPoints, currentPage: PaginationMetadata): Maybe<string> =>
  navigatePageinatedEndpoint('next', endpoint, currentPage);

export const urlForPreviousPage = (endpoint: EndPoints, currentPage: PaginationMetadata): Maybe<string> =>
  navigatePageinatedEndpoint('previous', endpoint, currentPage);

import {Period} from '../components/dates/dateModels';
import {SelectedParameters} from '../state/user-selection/userSelectionModels';
import {Pagination} from '../state/ui/pagination/paginationModels';
import {EncodedUriParameters, uuid} from '../types/Types';
import {currentDateRange, toApiParameters} from './dateHelpers';

interface ParameterNames {
  [key: string]: string;
}

export interface ParameterCallbacks {
  [key: string]: ((parameter: EncodedUriParameters) => string[]);
}

const parameterCallbacks: ParameterCallbacks = {
  period: (parameter: string) => toApiParameters(currentDateRange(parameter as Period)),
};

const baseParameterNames: ParameterNames = {
  cities: 'city',
  addresses: 'address',
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
  meterIds: 'id',
  meterStatuses: 'status',
  gatewayStatuses: 'gatewayStatus',
};

export type PaginatedParametersCombiner = (
  pagination: Pagination,
  selectedIds: SelectedParameters,
  callbacks?: ParameterCallbacks,
) => EncodedUriParameters;

export const encodedUriParametersForMeters: PaginatedParametersCombiner = (
  pagination: Pagination,
  selectedIds: SelectedParameters,
  callbacks: ParameterCallbacks = parameterCallbacks,
): EncodedUriParameters =>
  encodedUriParametersFrom({
    pagination,
    selectedIds,
    parameterNames: meterParameterNames,
    parameterCallbacks: callbacks,
  });

export const encodedUriParametersForGateways: PaginatedParametersCombiner = (
  pagination: Pagination,
  selectedIds: SelectedParameters,
  callbacks: ParameterCallbacks = parameterCallbacks,
): EncodedUriParameters =>
  encodedUriParametersFrom({
    pagination,
    selectedIds,
    parameterNames: gatewayParameterNames,
    parameterCallbacks: callbacks,
  });

export const encodedUriParametersForAllMeters = (
  selectedIds: SelectedParameters,
  callbacks: ParameterCallbacks = parameterCallbacks,
): EncodedUriParameters =>
  encodedUriParametersFrom({
    selectedIds,
    parameterNames: meterParameterNames,
    parameterCallbacks: callbacks,
  });

export const encodedUriParametersForAllGateways = (
  selectedIds: SelectedParameters,
  callbacks: ParameterCallbacks = parameterCallbacks,
): EncodedUriParameters =>
  encodedUriParametersFrom({
    selectedIds,
    parameterNames: gatewayParameterNames,
    parameterCallbacks: callbacks,
  });

export const encodedUriParametersForDashboard = (
  selectedIds: SelectedParameters,
  callbacks: ParameterCallbacks = parameterCallbacks,
): EncodedUriParameters =>
  encodedUriParametersFrom({
    selectedIds,
    parameterNames: meterParameterNames,
    parameterCallbacks: callbacks,
  });

interface UrlParameters {
  pagination?: Pagination;
  selectedIds: SelectedParameters;
  parameterNames: ParameterNames;
  parameterCallbacks: ParameterCallbacks;
}

const encodedUriParametersFrom =
  (
    {
      pagination,
      selectedIds,
      parameterNames,
      parameterCallbacks,
    }: UrlParameters,
  ): EncodedUriParameters => {
    const parameters: EncodedUriParameters[] = [];

    if (pagination) {
      const {page, size} = pagination;
      parameters.push(`size=${encodeURIComponent(size.toString())}`);
      parameters.push(`page=${encodeURIComponent(page.toString())}`);
    }

    const addParameterWith = (name: string, value: uuid | Period) =>
      parameters.push(`${parameterNames[name]}=${encodeURIComponent(value.toString())}`);

    Object.keys(selectedIds)
      .forEach((parameter: string) => {
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

export const makeUrl =
  (endpoint: string, parameters?: EncodedUriParameters): EncodedUriParameters => {
    if (parameters && parameters.length) {
      return endpoint + '?' + parameters;
    } else {
      return endpoint;
    }
  };

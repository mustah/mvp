import {Omit} from 'react-redux-typescript';
import {Pagination} from '../state/ui/pagination/paginationModels';
import {SelectedParameters} from '../state/user-selection/userSelectionModels';
import {EncodedUriParameters, uuid} from '../types/Types';

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

export const toEntityApiParametersMeters = (selectionParameters: Omit<SelectedParameters, 'dateRange'>) =>
  toEntityApiParameters(selectionParameters, meterParameterNames);

export const toEntityApiParametersGateways = (selectionParameters: Omit<SelectedParameters, 'dateRange'>) =>
  toEntityApiParameters(selectionParameters, gatewayParameterNames);

const toEntityApiParameters =
  // TODO: perhaps make sure it could handle if dateRange is included, as it is now the function would most likely fail.
  (
    selectionParameters: Omit<SelectedParameters, 'dateRange'>,
    parameterNames: ParameterNames,
  ): EncodedUriParameters[] =>
    Object.keys(selectionParameters)
      .reduce((prev: EncodedUriParameters[], parameter: string) =>
        [...prev,
          ...selectionParameters[parameter]
            .map((value: uuid) => `${parameterNames[parameter]}=${encodeURIComponent(value.toString())}`),
        ], []);

export const makeUrl =
  (endpoint: string, parameters?: EncodedUriParameters): EncodedUriParameters =>
    parameters && parameters.length ? endpoint + '?' + parameters : endpoint;

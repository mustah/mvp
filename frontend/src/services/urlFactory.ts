import {SelectedParameters} from '../state/search/selection/selectionModels';
import {uuid} from '../types/Types';

interface ParameterNames {
  [key: string]: string;
}

const baseParameterNames: ParameterNames = {
  cities: 'city.id',
  addresses: 'address.id',
  manufacturers: 'manufacturer',
  productModels: 'productModel',
  period: 'period',
  alarms: 'alarm',
};

const gatewayParameterNames: ParameterNames = {
  ...baseParameterNames,
  gatewayStatuses: 'status.id',
  meterStatuses: 'meterStatus.id',
};

const meterParameterNames: ParameterNames = {
  ...baseParameterNames,
  gatewayStatuses: 'gatewayStatus.id',
  meterStatuses: 'status.id',
};

export const encodedUriParametersForMeters = (selectedIds: SelectedParameters): string => {
  return encodedUriParametersFrom(selectedIds, meterParameterNames);
};

export const encodedUriParametersForGateways = (selectedIds: SelectedParameters): string => {
  return encodedUriParametersFrom(selectedIds, gatewayParameterNames);
};

const encodedUriParametersFrom = (selectedIds: SelectedParameters, parameterNames: ParameterNames): string => {
  const parameters: string[] = [];

  const addParameterWith = (name: string, value: any) => {
    parameters.push((parameterNames[name]) + '=' + encodeURIComponent(value.toString()));
  };

  Object.keys(selectedIds).forEach((name: string) => {
    const selection = selectedIds[name];
    if (Array.isArray(selection)) {
      selection.forEach((value: uuid) => addParameterWith(name, value));
    } else {
      addParameterWith(name, selection);
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

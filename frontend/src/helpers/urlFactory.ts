import {Period} from '../components/dates/dateModels';
import {SelectedParameters} from '../state/search/selection/selectionModels';
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

export const encodedUriParametersForMeters = (selectedIds: SelectedParameters): string => {
  return encodedUriParametersFrom(selectedIds, meterParameterNames, parameterCallbacks);
};

export const encodedUriParametersForGateways = (selectedIds: SelectedParameters): string => {
  return encodedUriParametersFrom(selectedIds, gatewayParameterNames, parameterCallbacks);
};

const encodedUriParametersFrom =
  (selectedIds: SelectedParameters, parameterNames: ParameterNames, parameterCallbacks: ParameterCallbacks): string => {
    const parameters: string[] = [];

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

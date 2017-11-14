import {SelectedParameters} from '../state/search/selection/selectionModels';
import {uuid} from '../types/Types';

const parameterNames = {
  cities: 'city.id',
  addresses: 'address.id',
  statuses: 'status',
  alarms: 'alarm',
  period: 'period',
};

export const encodedUriParametersFrom = (selectedIds: SelectedParameters): string => {
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

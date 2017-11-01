import {SelectedIds} from '../state/search/selection/selectionModels';
import {uuid} from '../types/Types';

const parameterAttributes = {
  cities: 'city',
  addresses: 'address',
  statuses: 'status',
};

export const encodedUriParametersFrom = (selectedIds: SelectedIds): string => {
  const parameters: string[] = [];
  Object.keys(selectedIds).forEach((key: string) => {
    selectedIds[key].forEach((id: uuid) => {
      const parameterName = parameterAttributes[key] || key;
      parameters.push(parameterName + '=' + encodeURIComponent(id.toString()));
    });
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

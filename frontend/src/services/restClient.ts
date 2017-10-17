import axios, {AxiosInstance} from 'axios';
import {config} from '../config/config';

const axiosConfig = config().axios;

export let restClient: AxiosInstance = axios.create(axiosConfig);

export const initRestClient = (token?: string): void => {
  if (token) {
    makeRestClient(token);
  }
};

export const makeRestClient = (token: string): AxiosInstance => {
  restClient = axios.create({
    ...axiosConfig,
    headers: {Authorization: `Basic ${token}`},
  });
  return restClient;
};

// TODO this is adapted for JSON server, will need to remake it for the real MVP back end
export const filterToUri = (endpoint: string, parameters: any): string => {
  const filters: string[] = [];

  Object.keys(parameters).forEach((key) => {
    if (parameters[key] instanceof Set) {
      parameters[key].forEach((value) => {
        filters.push(key + '=' + encodeURIComponent(value));
      });
    } else {
      filters.push(key + '=' + encodeURIComponent(parameters[key]));
    }
  });
  return endpoint + '?' + filters!.join('&');
};

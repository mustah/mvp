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
export const filterToUri = (endpoint: string, filter: any): string => {
  const filters: string[] = [];
  Object.keys(filter).forEach((key) => {
    // TODO replace the "I think every value in the filter object is a string" with typing,
    // when we set the real format of filter.. because this will
    // blow up if typeof filter[key] !== 'string'
    filters.push(key + "=" + encodeURIComponent(filter[key]));
  });
  return endpoint + '?' + filters!.join('&');
};

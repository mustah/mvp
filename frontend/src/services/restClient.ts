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

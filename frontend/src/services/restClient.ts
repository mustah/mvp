import axios, {AxiosInstance} from 'axios';
import {config} from '../config';

export let restClient: AxiosInstance = axios.create(config.axios);

export const initRestClient = (token?: string): void => {
  if (token) {
    makeRestClient(token);
  }
};

export const makeRestClient = (token: string): AxiosInstance => {
  restClient = axios.create({
    ...config.axios,
    headers: {Authorization: `Basic ${token}`},
  });
  return restClient;
};

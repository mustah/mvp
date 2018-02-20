import axios, {AxiosInstance} from 'axios';
import {config} from '../config/config';

const axiosConfig = config().axios;

export let restClient: AxiosInstance = axios.create(axiosConfig);

interface Headers {
  Authorization: string;
}

const makeRestClient = (headers: Headers): AxiosInstance => {
  restClient = axios.create({
    ...axiosConfig,
    headers,
  });
  return restClient;
};

export const restClientWith = (token?: string): void => {
  if (token) {
    makeRestClient({Authorization: `Bearer ${token}`});
  }
};

export const authenticate = (token: string): AxiosInstance =>
  makeRestClient({Authorization: `Basic ${token}`});

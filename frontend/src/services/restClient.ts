import axios, {AxiosInstance} from 'axios';
import {config} from '../config/config';
import {Unauthorized} from '../usecases/auth/authModels';

const axiosConfig = config().axios;

export let restClient: AxiosInstance = axios.create(axiosConfig);

interface Headers {
  Authorization: string;
}

export class InvalidToken implements Error, Unauthorized {

  name: string;
  message: string;
  timestamp: number;
  error: string;
  path: string;
  status: number;

  constructor(message: string) {
    this.message = message;
    this.name = 'InvalidToken';
    this.timestamp = -1;
  }
}

interface Headers {
  Authorization: string;
}

const makeRestClient = (headers: Headers): AxiosInstance => {
  restClient = axios.create({
    ...axiosConfig,
    headers,
  });
  setResponseInterceptors();

  return restClient;
};

const setResponseInterceptors = (): void => {
  restClient.interceptors.response.use((response) => response, (error) => {
    const {response} = error;
    if (response && response.data && response.data.message && response.data.message === 'Token missing or invalid') {
      return Promise.reject(new InvalidToken(response.data.message)); // TODO: Perhaps set a translated string here.
    } else {
      return Promise.reject(error);
    }
  });
};

export const restClientWith = (token?: string): void => {
  if (token) {
    makeRestClient({Authorization: `Bearer ${token}`});
  }
};

export const authenticate = (token: string): AxiosInstance =>
  makeRestClient({Authorization: `Basic ${token}`});

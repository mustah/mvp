import axios, {AxiosInstance} from 'axios';

const config = {
  baseURL: 'http://localhost:8080/api',
  timeout: 30000,
};

export let restClient: AxiosInstance = axios.create(config);

export const initRestClient = (token?: string): void => {
  if (token) {
    makeRestClient(token);
  }
};

export const makeRestClient = (token: string): AxiosInstance => {
  restClient = axios.create({
    ...config,
    headers: {Authorization: `Basic ${token}`},
  });
  return restClient;
};

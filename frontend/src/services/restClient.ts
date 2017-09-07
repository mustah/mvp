import axios, {AxiosInstance} from 'axios';

export const baseURL = 'http://localhost:8080/api';

const config = {
  baseURL,
  timeout: 30000,
};

export let restClient: AxiosInstance = axios.create(config);

export const createRestClient = (token: string): AxiosInstance => {
  restClient = axios.create({
    ...config,
    headers: {Authorization: `Basic ${token}`},
  });
  return restClient;
};

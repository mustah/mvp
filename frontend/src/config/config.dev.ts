import {AppConfig} from './AppConfig';

export const config: AppConfig = {
  environment: 'development',
  axios: {
    baseURL: '//localhost:8080/v1/api',
    timeout: 30000,
  },
};

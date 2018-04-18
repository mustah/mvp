import {AppConfig} from './AppConfig';

export const config: AppConfig = {
  environment: 'development',
  axios: {
    baseURL: `//${location.hostname}:8080/api/v1`,
    timeout: 30000,
  },
};

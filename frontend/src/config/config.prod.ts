import {AppConfig} from './AppConfig';

export const config: AppConfig = {
  environment: 'production',
  axios: {
    baseURL: '//' + location.host + '/v1/api',
    timeout: 30000,
  },
};

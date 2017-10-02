import {AppConfig} from './AppConfig';

export const config: AppConfig = {
  environment: 'production',
  axios: {
    baseURL: '//' + location.host + '/api',
    timeout: 30000,
  },
};

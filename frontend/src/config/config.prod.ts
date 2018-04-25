import {AppConfig} from './AppConfig';

export const config: AppConfig = {
  environment: 'production',
  axios: {
    baseURL: '//' + location.host + '/api/v1',
    timeout: 30000,
  },
  frontendVersion: 'FRONTEND_VERSION',
};

import {AppConfig} from './AppConfig';

export const config: AppConfig = {
  environment: 'production',
  useJsonServerInsteadOfJavaBackend: true,
  axios: {
    baseURL: '//' + location.host + '/api',
    timeout: 30000,
  },
};

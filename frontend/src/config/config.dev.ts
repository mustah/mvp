import {AppConfig} from './AppConfig';

export const config: AppConfig = {
  environment: 'development',
  useJsonServerInsteadOfJavaBackend: true,
  axios: {
    baseURL: '//localhost:8080/api',
    timeout: 30000,
  },
};

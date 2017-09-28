import {LazyAppConfig} from './types/AppConfig';

export const config: LazyAppConfig = () => {
  return {
    axios: {
      baseURL: '//' + location.host + '/api',
      timeout: 30000,
    },
  };
};

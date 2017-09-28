import {LazyAppConfig} from './types/AppConfig';

export const config: LazyAppConfig = () => {
  return {
    axios: {
      baseURL: '//localhost:8080/api',
      timeout: 30000,
    },
  };
};

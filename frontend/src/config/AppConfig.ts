interface AxiosConfig {
  baseURL: string;
  timeout: number;
}

/**
 * This is intended to represent the complete set of configuration
 * options for the front end part of MVP.
 */
export interface AppConfig {
  axios: AxiosConfig;
  environment: 'development' | 'production';
  frontendVersion: string;
}

/**
 * We want the configuration to be able to depend on run-time evaluation,
 * for example by lazily evaluating location.host.
 */
export type LazyAppConfig = () => AppConfig;

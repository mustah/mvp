import {LazyAppConfig} from './AppConfig';

/**
 * A LazyAppConfig is a factory method for obtaining n application
 * configuration suitable for the current environment
 * (think: prod. vs dev.)
 */
export const config: LazyAppConfig = () => {
  return process.env.NODE_ENV === 'development'
    ? require('./config.dev').config
    : require('./config.prod').config;
};

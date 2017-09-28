import {LazyAppConfig} from './types/AppConfig';

const config: LazyAppConfig = process.env.NODE_ENV === 'development'
  ? require('./config.dev')
  : require('./config.prod').config;

export {config};

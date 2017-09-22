import * as i18n from 'i18next';
import * as Backend from 'i18next-xhr-backend';
import {Language} from '../usecases/topmenu/containers/languageReducer';
const {i18nextConfig} = require('./i18nextConfig');

export const initLanguage = (language: Language) => {
  i18n
    .use(Backend)
    .init({
      lng: language.code,
      debug: true,
      ...i18nextConfig,
      backend: {
        loadPath: 'i18n/locales/{{lng}}.json',
      },
    });
};

export default i18n;

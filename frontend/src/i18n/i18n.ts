import * as i18n from 'i18next';
import * as Backend from 'i18next-xhr-backend';
import {LanguageCode} from '../state/language/languageModels';

const {i18nextConfig} = require('./i18nextConfig');

export const initLanguage = (language: LanguageCode) => {
  i18n
    .use(Backend)
    .init({
      lng: language,
      debug: false,
      ...i18nextConfig,
      backend: {
        loadPath: 'i18n/locales/{{lng}}.json',
      },
    });
};

export default i18n;

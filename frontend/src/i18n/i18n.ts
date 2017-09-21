import * as i18n from 'i18next';
import * as LanguageDetector from 'i18next-browser-languagedetector';
import * as Backend from 'i18next-xhr-backend';

const {i18nextConfig} = require('./i18nextConfig');

i18n
    .use(Backend)
    .use(LanguageDetector)
    .init({
              debug: true,
              ...i18nextConfig,
              backend: {
                  loadPath: 'i18n/locales/{{lng}}.json',
              },
          });

export default i18n;

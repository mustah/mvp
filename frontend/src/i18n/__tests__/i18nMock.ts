import * as i18n from 'i18next';
import {Dictionary} from '../../types/Types';

const {i18nextConfig} = require('../i18nextConfig');

export interface Translations {
  code: string;
  translation: Dictionary<string>;
}

export const initTranslations = ({code, translation}: Translations) => {
  i18n
    .init({
      lng: code,
      ...i18nextConfig,
      resources: {
        [code]: {
          translation,
        },
      },
    });
};

export const i18nMock = i18n;

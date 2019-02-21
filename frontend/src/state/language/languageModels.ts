import {Overwrite} from 'utility-types';
import {firstUpperTranslated} from '../../services/translationService';

export interface LanguageState {
  language: {code: LanguageCode};
}

export type LanguageCode = 'sv' | 'en';

export interface Language {
  name: string;
  code: LanguageCode;
}

export const languages: Readonly<Record<LanguageCode, Overwrite<Language, {name: () => string}>>> = {
  sv: {
    code: 'sv',
    name: () => firstUpperTranslated('swedish'),
  },
  en: {
    code: 'en',
    name: () => firstUpperTranslated('english'),
  },
};

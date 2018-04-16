import {Overwrite} from 'react-redux-typescript';
import {firstUpperTranslated} from '../../services/translationService';

export interface LanguageState {
  language: LanguageCode;
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

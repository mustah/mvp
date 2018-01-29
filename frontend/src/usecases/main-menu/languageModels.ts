import {ObjectsById} from '../../state/domain-models/domainModels';

export interface LanguageState {
  language: Language;
}

export interface Language {
  name: string;
  code: string;
}

export const supportedLanguages: Readonly<ObjectsById<Language>> = {
  sv: {
    code: 'sv',
    name: 'Swedish',
  },
  en: {
    code: 'en',
    name: 'English',
  },
};

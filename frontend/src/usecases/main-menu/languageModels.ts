import {DomainModel} from '../../state/domain-models/domainModels';

export interface LanguageState {
  language: Language;
}

export interface Language {
  name: string;
  code: string;
}

export const supportedLanguages: Readonly<DomainModel<Language>> = {
  sv: {
    code: 'sv',
    name: 'Swedish',
  },
  en: {
    code: 'en',
    name: 'English',
  },
};

import {AnyAction} from 'redux';
import {CHANGE_LANGUAGE} from './languageActions';
const {defaultLanguage} = require('../../../i18n/i18nextConfig');

export interface LanguageState {
  language: Language;
}

export interface Language {
  name: string;
  code: string;
}

export const supportedLanguages: {readonly [key: string]: Language} = {
  sv: {
    code: 'sv',
    name: 'Swedish',
  },
  en: {
    code: 'en',
    name: 'English',
  },
};

const initialState: LanguageState = {
  language: supportedLanguages[defaultLanguage],
};

export const language = (state: LanguageState = initialState, action: AnyAction) => {
  switch (action.type) {
    case CHANGE_LANGUAGE:
      return {
        ...state,
        language: action.payload,
      };
    default:
      return state;
  }
};

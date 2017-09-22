import {AnyAction} from 'redux';
import {CHANGE_LANGUAGE} from './languageActions';

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
  language: supportedLanguages.sv,
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

import {EmptyAction} from 'react-redux-typescript';
import {Action} from '../../types/Types';
import {CHANGE_LANGUAGE} from './languageActions';
import {Language, LanguageState, supportedLanguages} from './languageModels';

const initialState: LanguageState = {
  language: supportedLanguages.sv,
};

type ActionTypes = Action<Language> & EmptyAction<string>;

export const language = (state: LanguageState = initialState, action: ActionTypes) => {
  switch (action.type) {
    case CHANGE_LANGUAGE:
      return {
        ...state,
        language: (action as Action<Language>).payload,
      };
    default:
      return state;
  }
};

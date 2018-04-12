import {EmptyAction} from 'react-redux-typescript';
import {Action} from '../../types/Types';
import {CHANGE_LANGUAGE} from './languageActions';
import {LanguageCode, LanguageState, languages} from './languageModels';

const initialState: LanguageState = {
  language: languages.sv.code,
};

type ActionTypes = Action<LanguageCode> | EmptyAction<string>;

export const language = (state: LanguageState = initialState, action: ActionTypes) => {
  switch (action.type) {
    case CHANGE_LANGUAGE:
      return {
        ...state,
        language: (action as Action<LanguageCode>).payload,
      };
    default:
      return state;
  }
};

import {EmptyAction} from 'typesafe-actions/dist/types';
import {Action} from '../../types/Types';
import {CHANGE_LANGUAGE} from './languageActions';
import {LanguageCode, LanguageState, languages} from './languageModels';

const initialState: LanguageState = {
  language: {code: languages.en.code},
};

type ActionTypes = Action<LanguageCode> | EmptyAction<string>;

export const language = (state: LanguageState = initialState, action: ActionTypes) => {
  switch (action.type) {
    case CHANGE_LANGUAGE:
      return {
        ...state,
        language: {code: (action as Action<LanguageCode>).payload},
      };
    default:
      return state;
  }
};

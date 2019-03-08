import {getType} from 'typesafe-actions';
import {EmptyAction} from 'typesafe-actions/dist/types';
import {Action} from '../../types/Types';
import {changeLanguageRequest} from './languageActions';
import {LanguageCode, languages, LanguageState} from './languageModels';

const initialState: LanguageState = {
  language: {code: languages.en.code},
};

type ActionTypes = Action<LanguageCode> | EmptyAction<string>;

export const language = (state: LanguageState = initialState, action: ActionTypes) => {
  switch (action.type) {
    case getType(changeLanguageRequest):
      return {
        ...state,
        language: {code: (action as Action<LanguageCode>).payload},
      };
    default:
      return state;
  }
};

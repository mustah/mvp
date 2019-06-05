import {ActionType, getType} from 'typesafe-actions';
import {unknownAction} from '../ui/tabs/tabsActions';
import {changeLanguageRequest} from './languageActions';
import {languages, LanguageState} from './languageModels';

const initialState: LanguageState = {
  language: {code: languages.en.code},
};

type ActionTypes = ActionType<typeof changeLanguageRequest | typeof unknownAction>;

export const language = (state: LanguageState = initialState, action: ActionTypes) => {
  if (action.type === getType(changeLanguageRequest)) {
    return {...state, language: {code: action.payload}};
  } else {
    return state;
  }
};

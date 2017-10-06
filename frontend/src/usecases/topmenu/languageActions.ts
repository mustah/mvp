import {createPayloadAction} from 'react-redux-typescript';
import {changeTranslationLanguage} from '../../services/translationService';
import {Language} from './languageReducer';

export const CHANGE_LANGUAGE = 'CHANGE_LANGUAGE';

const changeLanguageRequest = createPayloadAction(CHANGE_LANGUAGE);

export const changeLanguage = (language: Language) => {
  return (dispatch) => {
    changeTranslationLanguage(language.code, () => {
      dispatch(changeLanguageRequest(language));
    });
  };
};

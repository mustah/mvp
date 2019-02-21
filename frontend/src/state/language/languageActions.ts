import {GetState} from '../../reducers/rootReducer';
import {changeTranslationLanguage, getI18nLanguage} from '../../services/translationService';
import {Callback, payloadActionOf} from '../../types/Types';
import {LanguageCode} from './languageModels';

export const CHANGE_LANGUAGE = 'CHANGE_LANGUAGE';

const changeLanguageRequest = payloadActionOf<LanguageCode>(CHANGE_LANGUAGE);

const reloadPage = () => window.location.reload();
export const changeLanguage = (language: LanguageCode, onComplete: Callback = reloadPage) =>
  (dispatch, getState: GetState) => {
    const {language: stateLanguage} = getState().language;
    if (stateLanguage.code !== language || getI18nLanguage() !== language) {
      changeTranslationLanguage(language, () => {
        dispatch(changeLanguageRequest(language));
        onComplete();
      });
    }
  };

import {createStandardAction} from 'typesafe-actions';
import {GetState} from '../../reducers/rootReducer';
import {changeTranslationLanguage, getI18nLanguage} from '../../services/translationService';
import {Callback} from '../../types/Types';
import {LanguageCode} from './languageModels';

export const changeLanguageRequest = createStandardAction('CHANGE_LANGUAGE_REQUEST')<LanguageCode>();

const reloadPage = () => window.location.reload();

export const changeLanguage = (language: LanguageCode) => changeLanguageWithRefresh(language, () => {
  /* ignore */
});

export const changeLanguageWithRefresh = (language: LanguageCode, onComplete: Callback = reloadPage) =>
  async (dispatch, getState: GetState) => {
    const {language: stateLanguage} = getState().language;
    if (stateLanguage.code !== language || getI18nLanguage() !== language) {
      try {
        await changeTranslationLanguage(language, () => {
          dispatch(changeLanguageRequest(language));
        });

        onComplete();
      } catch (e) {/* ignore */}
    }
  };

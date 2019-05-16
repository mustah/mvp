import axios from 'axios';
import {default as MockAdapter} from 'axios-mock-adapter';
import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {i18nMock, initTranslations} from '../../../i18n/__tests__/i18nMock';
import {authenticate} from '../../../services/restClient';
import {changeLanguageWithRefresh, changeLanguageRequest} from '../languageActions';
import {languages, LanguageState} from '../languageModels';

describe('languageActions', () => {
  const configureMockStore = configureStore([thunk]);
  let mockRestClient: MockAdapter;
  let store;
  const initialLanguage = languages.en.code;

  beforeEach(() => {
    const initialLanguageState: LanguageState = {language: {code: initialLanguage}};
    store = configureMockStore({language: initialLanguageState});
    mockRestClient = new MockAdapter(axios);
    authenticate('test');
    initTranslations({
        code: initialLanguage,
        translation: {
          'token missing or invalid': 'token must be missing',
          'bad credentials': 'no password',
          'ok': 'ok status',
          'warning': 'not good',
          'unknown': 'do not know this',
        },
      },
    );
  });

  afterEach(() => {
    mockRestClient.reset();
  });

  it('has initial language', () => {
    expect(i18nMock.language).toEqual('en');
  });

  describe('updates language for both i18n and state when requested language differs from either', () => {

    it('updates language when requested language differs from redux state language', () => {
      store.dispatch(changeLanguageWithRefresh(languages.sv.code, () => void(0)));

      expect(store.getActions()).toEqual([changeLanguageRequest('sv')]);
      expect(i18nMock.language).toEqual('sv');
    });

    it('updates language when requested language differs from i18n language', () => {
      store = configureMockStore({language: {language: 'sv'}});
      store.dispatch(changeLanguageWithRefresh(languages.sv.code, () => void(0)));

      expect(store.getActions()).toEqual([changeLanguageRequest('sv')]);
      expect(i18nMock.language).toEqual('sv');
    });
  });

  it('does not update i18n or language state if requested language is current language', () => {
    let hasBeenCalled = '';
    const onCompleteExecuted = () => hasBeenCalled = 'onComplete executed';
    store.dispatch(changeLanguageWithRefresh(initialLanguage, onCompleteExecuted));

    expect(store.getActions()).toEqual([]);
    expect(i18nMock.language).toEqual('en');
    expect(hasBeenCalled).toEqual('');
  });
});

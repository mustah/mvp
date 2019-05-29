import {default as MockAdapter} from 'axios-mock-adapter';
import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {makeUser} from '../../../__tests__/testDataFactory';
import {makeThemeUrlOf, makeUpdateThemeUrlOf, makeUrl} from '../../../helpers/urlFactory';
import {initTranslations} from '../../../i18n/__tests__/i18nMock';
import {EndPoints} from '../../../services/endPoints';
import {restClient, restClientWith} from '../../../services/restClient';
import {User} from '../../../state/domain-models/user/userModels';
import {Callback, ErrorResponse} from '../../../types/Types';
import {AuthState} from '../../auth/authModels';
import {
  changePrimaryColor,
  changeSecondaryColor,
  failureTheme,
  fetchTheme,
  makeBody,
  requestTheme,
  resetColors,
  successTheme
} from '../themeActions';
import {ThemeRequestModel} from '../themeModels';
import {initialState} from '../themeReducer';

describe('themeActions', () => {

  initTranslations({
    code: 'en',
    translation: {
      test: 'no translations will default to key',
    },
  });

  let store;
  let mockRestClient;
  const configureMockStore = configureStore([thunk]);

  beforeEach(() => {
    restClientWith('someToken');
    mockRestClient = new MockAdapter(restClient);
  });

  describe('fetch theme', () => {

    beforeEach(() => {
      store = configureMockStore({theme: {...initialState}});
    });

    it('will fail when if fetching does not return ok status', async () => {
      const onFetchWillFail = async (endpoint: EndPoints | string) => {
        const response: ErrorResponse = {message: 'request failed'};
        mockRestClient.onGet(makeUrl(endpoint)).reply(401, response);
        return store.dispatch(fetchTheme(endpoint));
      };

      await onFetchWillFail(makeThemeUrlOf('slug'));

      expect(store.getActions()).toEqual([
        requestTheme(),
        failureTheme({message: 'Request failed'}),
      ]);
    });

    it('fetches only primary key and color value from server', async () => {
      const response: any[] = [{key: 'primary', value: 'blue'}];

      await onFetch(makeThemeUrlOf('slug'), response);

      expect(store.getActions()).toEqual([
        requestTheme(),
        successTheme({primary: 'blue', secondary: initialState.color.secondary}),
      ]);
    });

    it('fetches only secondary key and color value from server', async () => {
      const response: any[] = [{key: 'secondary', value: 'white'}];

      await onFetch(makeThemeUrlOf('slug'), response);

      expect(store.getActions()).toEqual([
        requestTheme(),
        successTheme({secondary: 'white', primary: initialState.color.primary}),
      ]);
    });

    it('fetches primary and secondary', async () => {
      const response = [{key: 'primary', value: '#ffffff'}, {key: 'secondary', value: '#cccccc'}];

      await onFetch(makeThemeUrlOf('slug'), response);

      expect(store.getActions()).toEqual([
        requestTheme(),
        successTheme({primary: '#ffffff', secondary: '#cccccc'}),
      ]);
    });

    it('fetches primary and secondary and ignore others', async () => {
      const response = [
        {key: 'primary', value: '#ffffff'},
        {key: 'secondary', value: '#cccccc'},
        {key: 'third', value: 'red'}
      ];

      await onFetch(makeThemeUrlOf('slug'), response);

      expect(store.getActions()).toEqual([
        requestTheme(),
        successTheme({primary: '#ffffff', secondary: '#cccccc'}),
      ]);
    });

    const onFetch = async (endpoint: EndPoints | string, response: any) => {
      mockRestClient.onGet(makeUrl(endpoint)).reply(200, response);
      return store.dispatch(fetchTheme(endpoint));
    };
  });

  describe('update theme', () => {
    const user: User = {...makeUser(), language: 'sv'};
    const auth: AuthState = {user, isAuthenticated: true};

    beforeEach(() => {
      store = configureMockStore({auth, theme: initialState});
    });

    it('can reset primary and secondary colors to evo colors', async () => {
      const {color: {primary, secondary}} = initialState;
      const response: ThemeRequestModel[] = makeBody(primary, secondary);

      await onUpdate(response, resetColors);

      expect(store.getActions()).toEqual([
        requestTheme(),
        successTheme({primary, secondary}),
      ]);
    });

    it('can change primary color and use default secondary color', async () => {
      const {color: {secondary}} = initialState;
      const response: ThemeRequestModel[] = makeBody('blue', secondary);

      await onUpdate(response, () => changePrimaryColor('blue', 1));

      expect(store.getActions()).toEqual([
        requestTheme(),
        successTheme({primary: 'blue', secondary}),
      ]);
    });

    it('can change secondary color and use default primary color', async () => {
      const {color: {primary}} = initialState;
      const response: ThemeRequestModel[] = makeBody(primary, 'red');

      await onUpdate(response, () => changeSecondaryColor('red', 1));

      expect(store.getActions()).toEqual([
        requestTheme(),
        successTheme({primary, secondary: 'red'}),
      ]);
    });

    const onUpdate = async (body: ThemeRequestModel[], callback: Callback) => {
      const url = makeUpdateThemeUrlOf(user.organisation.id);
      mockRestClient.onPut(url, body).reply(200, body);
      return store.dispatch(callback());
    };

  });

});

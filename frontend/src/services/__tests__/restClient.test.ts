import {routerActions} from 'react-router-redux';
import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {routes} from '../../app/routes';
import {initLanguage} from '../../i18n/i18n';
import {Meter} from '../../state/domain-models-paginated/meter/meterModels';
import {EndPoints, HttpMethod, Normalized} from '../../state/domain-models/domainModels';
import {requestMethod} from '../../state/domain-models/domainModelsActions';
import {initialDomain} from '../../state/domain-models/domainModelsReducer';
import {fetchAllMeters} from '../../state/domain-models/meter-all/allMetersApiActions';
import {User} from '../../state/domain-models/user/userModels';
import {logoutUser} from '../../usecases/auth/authActions';
import {Unauthorized} from '../../usecases/auth/authModels';
import {authenticate, InvalidToken, restClient, restClientWith} from '../restClient';
import MockAdapter = require('axios-mock-adapter');

describe('restClient', () => {

  describe('restClientWith', () => {
    it('does not create any new restClient instance when no token exists', () => {
      restClientWith(undefined);

      expect(restClient.defaults.headers.Authorization).toBeUndefined();
    });

    it('creates restClient with bearer authentication header', () => {
      restClientWith('123-1234');

      expect(restClient.defaults.headers.Authorization).toBe('Bearer 123-1234');
    });

    it('creates restClient with basic authentication header', () => {
      authenticate('81818-19911');

      expect(restClient.defaults.headers.Authorization).toBe('Basic 81818-19911');
    });
  });

  describe('token invalid', () => {

    const configureMockStore = configureStore([thunk]);
    let store;

    it('dispatches an action if token is invalid', async () => {
      restClientWith('123123123');
      initLanguage({code: 'en', name: 'english'});
      const meterRequest = requestMethod<Normalized<Meter>>(EndPoints.allMeters, HttpMethod.GET);
      const mockRestClient = new MockAdapter(restClient);
      const user: User = {
        id: 1,
        name: 'al',
        email: 'al@la.se',
        organisation: {id: 1, name: 'elvaco', code: 'elvaco'},
        roles: [],
      };
      const initialState = {
        domainModels: {allMeters: initialDomain()}, auth: {
          user,
          isAuthenticated: true,
        },
      };
      store = configureMockStore(initialState);
      const error = new InvalidToken('Token missing or invalid');
      const getMetersInvalidToken = async () => {
        mockRestClient.onGet(EndPoints.allMeters).reply(401, error);
        mockRestClient.onGet(EndPoints.logout).reply(204);
        return store.dispatch(fetchAllMeters());
      };

      await getMetersInvalidToken();

      expect(store.getActions()).toEqual([
        meterRequest.request(),
        logoutUser(error as Unauthorized),
        routerActions.push(`${routes.login}/${initialState.auth.user.organisation.code}`),
      ]);
    });
  });
});

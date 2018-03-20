import {routerActions} from 'react-router-redux';
import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {routes} from '../../../../app/routes';
import {initLanguage} from '../../../../i18n/i18n';
import {EndPoints} from '../../../../services/endPoints';
import {InvalidToken, restClient, restClientWith} from '../../../../services/restClient';
import {logoutUser} from '../../../../usecases/auth/authActions';
import {Unauthorized} from '../../../../usecases/auth/authModels';
import {Meter} from '../../../domain-models-paginated/meter/meterModels';
import {Normalized} from '../../domainModels';
import {getRequestOf} from '../../domainModelsActions';
import {initialDomain} from '../../domainModelsReducer';
import {User} from '../../user/userModels';
import {fetchAllMeters} from '../allMetersApiActions';
import MockAdapter = require('axios-mock-adapter');

describe('allMeterApiActions', () => {
  describe('token invalid', () => {

    const configureMockStore = configureStore([thunk]);
    let store;

    it('dispatches an action if token is invalid', async () => {
      restClientWith('123123123');
      initLanguage({code: 'en', name: 'english'});
      const meterRequest = getRequestOf<Normalized<Meter>>(EndPoints.allMeters);
      const mockRestClient = new MockAdapter(restClient);
      const user: User = {
        id: 1,
        name: 'al',
        email: 'al@la.se',
        organisation: {id: 1, name: 'elvaco', slug: 'elvaco'},
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
        routerActions.push(`${routes.login}/${initialState.auth.user.organisation.slug}`),
      ]);
    });
  });
});

import axios from 'axios';
import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {initLanguage} from '../../../../i18n/i18n';
import {EndPoints} from '../../../../services/endPoints';
import {authenticate} from '../../../../services/restClient';
import {showFailMessage, showSuccessMessage} from '../../../ui/message/messageActions';
import {DomainModelsState} from '../../domainModels';
import {postRequestOf} from '../../domainModelsActions';
import {initialDomain} from '../../domainModelsReducer';
import {Organisation} from '../organisationModels';
import {addOrganisation} from '../organisationsApiActions';
import MockAdapter = require('axios-mock-adapter');

const configureMockStore = configureStore([thunk]);

describe('organisationsApiActions', () => {

  initLanguage({code: 'en', name: 'english'});

  const createOrganisation = postRequestOf<Organisation>(EndPoints.organisations);

  let mockRestClient: MockAdapter;
  let store;

  beforeEach(() => {
    const initialState: Partial<DomainModelsState> = {
      organisations: {...initialDomain()},
    };
    store = configureMockStore({domainModels: initialState});
    mockRestClient = new MockAdapter(axios);
    authenticate('test');
  });

  afterEach(() => {
    mockRestClient.reset();
  });

  describe('add new organisation', () => {

    const newOrganisation: Partial<Organisation> = {
      name: 'Hällesåkers IF',
      code: 'HIF',
    };
    const returnedOrganisation: Partial<Organisation> = {...newOrganisation, id: 1};
    const errorResponse = {message: 'An error'};

    const postOrganisationWithResponseOk = async (organisation: Partial<Organisation>) => {
      mockRestClient.onPost(EndPoints.organisations, organisation).reply(200, returnedOrganisation);
      return store.dispatch(addOrganisation(organisation as Organisation));
    };
    const postUserWithBadRequest = async (organisation: Partial<Organisation>) => {
      mockRestClient.onPost(EndPoints.organisations, organisation).reply(401, errorResponse);
      return store.dispatch(addOrganisation(organisation as Organisation));
    };

    it('sends a post request to backend and get a user with an id back', async () => {
      await postOrganisationWithResponseOk(newOrganisation);

      expect(store.getActions()).toEqual([
        createOrganisation.request(),
        createOrganisation.success(returnedOrganisation as Organisation),
        showSuccessMessage('Successfully created the organisation ' +
                           `${returnedOrganisation.name} (${returnedOrganisation.code})`),
      ]);
    });

    it('send a post request to backend and get an error back', async () => {
      await postUserWithBadRequest(newOrganisation);

      expect(store.getActions()).toEqual([
        createOrganisation.request(),
        createOrganisation.failure({...errorResponse}),
        showFailMessage(`Failed to create organisation: ${errorResponse.message}`),
      ]);
    });
  });

});

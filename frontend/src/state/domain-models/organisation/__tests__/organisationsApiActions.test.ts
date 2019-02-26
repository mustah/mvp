import axios from 'axios';
import {default as MockAdapter} from 'axios-mock-adapter';
import {routerActions} from 'react-router-redux';
import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {Overwrite} from 'utility-types';
import {routes} from '../../../../app/routes';
import {idGenerator} from '../../../../helpers/idGenerator';
import {initTranslations} from '../../../../i18n/__tests__/i18nMock';
import {EndPoints} from '../../../../services/endPoints';
import {authenticate} from '../../../../services/restClient';
import {showFailMessage, showSuccessMessage} from '../../../ui/message/messageActions';
import {DomainModelsState} from '../../domainModels';
import {postRequestOf, putRequestOf} from '../../domainModelsActions';
import {initialDomain} from '../../domainModelsReducer';
import {Organisation, OrganisationWithoutId} from '../organisationModels';
import {addOrganisation, addSubOrganisation, updateOrganisation} from '../organisationsApiActions';

const configureMockStore = configureStore([thunk]);

describe('organisationsApiActions', () => {

  initTranslations({
    code: 'en',
    translation: {
      test: 'no translations will default to key',
    },
  });

  const createOrganisation = postRequestOf<Organisation>(EndPoints.organisations);
  const putOrganisation = putRequestOf<Organisation>(EndPoints.organisations);

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

  describe('addOrganisation', () => {

    const newOrganisation: Partial<Organisation> = {
      name: 'Hällesåkers IF',
      slug: 'HIF',
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
                           `${returnedOrganisation.name} (${returnedOrganisation.slug})`),
        routerActions.push(`${routes.adminOrganisations}`)
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

  describe('addSubOrganisation', () => {

    type UnsavedOrganisationWithParent = Overwrite<OrganisationWithoutId, {parent: Organisation}>;

    const newSubOrganisation: UnsavedOrganisationWithParent = {
      name: 'Hällesåkers IF',
      slug: 'HIF',
      parent: {
        name: 'Höganäs BK',
        slug: 'HBK',
        id: idGenerator.uuid(),
      },
    };
    const returnedOrganisation: Organisation = {...newSubOrganisation, id: 1};

    const postSubOrganisationWithResponseOk = async (organisation: UnsavedOrganisationWithParent) => {
      mockRestClient.onPost(`${EndPoints.organisations}/${organisation.parent.id}/sub-organisations`, organisation)
        .reply(200, returnedOrganisation);
      return store.dispatch(addSubOrganisation(organisation, organisation.parent.id));
    };

    it('sends a post request to backend and get a user with an id back', async () => {
      await postSubOrganisationWithResponseOk(newSubOrganisation);

      const {name, slug} = returnedOrganisation;

      expect(store.getActions()).toEqual([
        createOrganisation.request(),
        createOrganisation.success(returnedOrganisation),
        showSuccessMessage(`Successfully created the organisation ${name} (${slug})`),
        routerActions.push(`${routes.adminOrganisations}`)
      ]);
    });

  });

  describe('updateOrganisation', () => {

    const existingOrganisation: Organisation = {
      id: idGenerator.uuid(),
      name: 'Hällesåkers IF',
      slug: 'HIF',
    };

    const existingSubOrganisation: Organisation = {
      id: idGenerator.uuid(),
      name: 'Örebro SK',
      slug: 'ÖSK',
      parent: {
        name: 'Höganäs BK',
        slug: 'HBK',
        id: idGenerator.uuid(),
      },
    };

    const putOrganisationWithResponseOk = async (organisation: Organisation) => {
      mockRestClient
        .onPut(`${EndPoints.organisations}`, organisation)
        .reply(200, organisation);
      return store.dispatch(updateOrganisation(organisation));
    };

    it('can update organisation', async () => {
      const updatedOrganisation: Organisation = {
        ...existingOrganisation,
        name: 'Märsta IF',
        slug: 'MIF',
      };

      await putOrganisationWithResponseOk(updatedOrganisation);

      const {name, slug} = updatedOrganisation;

      expect(store.getActions()).toEqual([
        putOrganisation.request(),
        putOrganisation.success(updatedOrganisation),
        showSuccessMessage(`Successfully updated the organisation ${name} (${slug})`),
      ]);
    });

    it('can update sub-organisation', async () => {
      const updatedOrganisation: Organisation = {
        ...existingSubOrganisation,
        name: 'Märsta IF',
        slug: 'MIF',
      };

      await putOrganisationWithResponseOk(updatedOrganisation);

      const {name, slug} = updatedOrganisation;

      expect(store.getActions()).toEqual([
        putOrganisation.request(),
        putOrganisation.success(updatedOrganisation),
        showSuccessMessage(`Successfully updated the organisation ${name} (${slug})`),
      ]);
    });

    it('can turn sub-organisation into organisation', async () => {
      const updatedOrganisation: Organisation = {...existingSubOrganisation};
      delete updatedOrganisation.parent;

      await putOrganisationWithResponseOk(updatedOrganisation);

      const {name, slug} = updatedOrganisation;

      expect(store.getActions()).toEqual([
        putOrganisation.request(),
        putOrganisation.success(updatedOrganisation),
        showSuccessMessage(`Successfully updated the organisation ${name} (${slug})`),
      ]);
    });

    it('can turn organisation into sub-organisation', async () => {
      const updatedOrganisation: Organisation = {
        ...existingOrganisation,
        parent: {
          ...existingSubOrganisation.parent!
        },
      };

      await putOrganisationWithResponseOk(updatedOrganisation);

      const {name, slug} = updatedOrganisation;

      expect(store.getActions()).toEqual([
        putOrganisation.request(),
        putOrganisation.success(updatedOrganisation),
        showSuccessMessage(`Successfully updated the organisation ${name} (${slug})`),
      ]);
    });

  });

});

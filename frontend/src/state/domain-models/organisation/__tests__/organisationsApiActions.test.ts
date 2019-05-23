import axios from 'axios';
import {default as MockAdapter} from 'axios-mock-adapter';
import {routerActions} from 'connected-react-router';
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
import {deleteRequestOf, postRequestOf, putRequestOf} from '../../domainModelsActions';
import {initialDomain} from '../../domainModelsReducer';
import {Organisation, OrganisationWithoutId} from '../organisationModels';
import {
  addOrganisation,
  addSubOrganisation,
  AssetTypeForOrganisation,
  OrganisationAssetType,
  resetAsset,
  updateOrganisation,
  uploadAsset
} from '../organisationsApiActions';

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
        showSuccessMessage('Created the organisation ' +
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
        showSuccessMessage(`Created the organisation ${name} (${slug})`),
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
        showSuccessMessage(`Updated the organisation ${name} (${slug})`),
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
        showSuccessMessage(`Updated the organisation ${name} (${slug})`),
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
        showSuccessMessage(`Updated the organisation ${name} (${slug})`),
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
        showSuccessMessage(`Updated the organisation ${name} (${slug})`),
      ]);
    });

  });

  describe('asset', () => {

    const params: AssetTypeForOrganisation = {
      assetType: OrganisationAssetType.logotype,
      organisationId: idGenerator.uuid(),
    };

    describe('uploadAsset', () => {

      const putAsset = async (params: AssetTypeForOrganisation, payload, responseCode: number) => {
        const {organisationId, assetType} = params;
        mockRestClient
          .onPut(`${EndPoints.organisations}/${organisationId}/assets/${assetType}`)
          .reply(responseCode);
        const formData = new FormData();
        formData.append(assetType, payload);
        return store.dispatch(uploadAsset(formData, params));
      };

      const fileToUpload = new Blob(['some file contents']);

      const putActions = putRequestOf<undefined>(EndPoints.organisations);

      it('handles a correct upload', async () => {
        await putAsset(params, fileToUpload, 200);

        expect(store.getActions())
          .toEqual([
            putActions.request(),
            putActions.success(undefined),
            showSuccessMessage('Updated'),
          ]);
      });

      it('handles upload error', async () => {
        await putAsset(params, fileToUpload, 400);

        expect(store.getActions())
          .toEqual([
            putActions.request(),
            putActions.failure({message: 'An unexpected error occurred'}),
            showFailMessage('Failed to update: An unexpected error occurred'),
          ]);
      });

    });

    describe('resetAsset', () => {

      const deleteAsset = async (params: AssetTypeForOrganisation, responseCode: number) => {
        const {organisationId, assetType} = params;
        mockRestClient
          .onDelete(`${EndPoints.organisations}/${organisationId}/assets/${assetType}`)
          .reply(responseCode);
        return store.dispatch(resetAsset(params));
      };

      const deleteActions = deleteRequestOf<undefined>(EndPoints.organisations);

      it('handles a correct upload', async () => {
        await deleteAsset(params, 200);

        expect(store.getActions())
          .toEqual([
            deleteActions.request(),
            deleteActions.success(undefined),
            showSuccessMessage('Now using default'),
          ]);
      });

      it('handles upload error', async () => {
        await deleteAsset(params, 500);

        expect(store.getActions())
          .toEqual([
            deleteActions.request(),
            deleteActions.failure({message: 'An unexpected error occurred'}),
            showFailMessage('Failed to update: An unexpected error occurred'),
          ]);
      });

    });

  });

});

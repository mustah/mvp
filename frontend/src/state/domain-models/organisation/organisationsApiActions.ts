import {Dispatch} from 'react-redux';
import {RootState} from '../../../reducers/rootReducer';
import {EndPoints} from '../../../services/endPoints';
import {firstUpperTranslated} from '../../../services/translationService';
import {CallbackOfData, CallbackOfDataAndUrlParameters, ErrorResponse, uuid} from '../../../types/Types';
import {showFailMessage, showSuccessMessage} from '../../ui/message/messageActions';
import {
  clearError,
  deleteRequest,
  fetchEntityIfNeeded,
  fetchIfNeeded,
  postRequest,
  postRequestToUrl
} from '../domainModelsActions';
import {Organisation, OrganisationWithoutId} from './organisationModels';
import {organisationsDataFormatter} from './organisationSchema';

export const clearOrganisationErrors = clearError(EndPoints.organisations);

export const fetchOrganisations = fetchIfNeeded<Organisation>(
  EndPoints.organisations,
  'organisations',
  organisationsDataFormatter,
);

export const fetchOrganisation = fetchEntityIfNeeded(EndPoints.organisations, 'organisations');

export const deleteOrganisation = deleteRequest<Organisation>(EndPoints.organisations, {
    afterSuccess: (organisation: Organisation, dispatch: Dispatch<RootState>) => {
      const translatedMessage = firstUpperTranslated(
        'successfully deleted the organisation {{name}}',
        {...organisation},
      );
      dispatch(showSuccessMessage(translatedMessage));
    },
    afterFailure: ({message}: ErrorResponse, dispatch: Dispatch<RootState>) => {
      const translatedMessage = firstUpperTranslated(
        'failed to delete the organisation: {{error}}',
        {error: message},
      );
      dispatch(showFailMessage(translatedMessage));
    },
  },
);

const organisationCallbacks = {
  afterSuccess: (organisation: Organisation, dispatch: Dispatch<RootState>) => {
    dispatch(showSuccessMessage(
      firstUpperTranslated(
        'successfully created the organisation {{name}} ({{slug}})',
        {...organisation},
      ),
    ));
  },
  afterFailure: ({message}: ErrorResponse, dispatch: Dispatch<RootState>) => {
    dispatch(showFailMessage(firstUpperTranslated(
      'failed to create organisation: {{error}}',
      {error: message},
    )));
  },
};

export const addOrganisation: CallbackOfData =
  postRequest<OrganisationWithoutId>(EndPoints.organisations, organisationCallbacks);

export const addSubOrganisation: CallbackOfDataAndUrlParameters =
  postRequestToUrl<OrganisationWithoutId, uuid>(
    EndPoints.organisations,
    organisationCallbacks,
    (parentId: uuid) => `${EndPoints.organisations}/${parentId}/sub-organisations`
  );

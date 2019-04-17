import {Dispatch} from 'react-redux';
import {routerActions} from 'react-router-redux';
import {routes} from '../../../app/routes';
import {RootState} from '../../../reducers/rootReducer';
import {EndPoints} from '../../../services/endPoints';
import {firstUpperTranslated} from '../../../services/translationService';
import {
  CallbackWithData,
  CallbackWithDataAndUrlParameters, emptyActionOf,
  ErrorResponse,
  Sectors,
  uuid
} from '../../../types/Types';
import {showFailMessage, showSuccessMessage} from '../../ui/message/messageActions';
import {
  clearError,
  deleteRequest, domainModelsClear,
  fetchEntityIfNeeded,
  fetchIfNeeded,
  fetchIfNeededForSector,
  postRequest,
  postRequestToUrl,
  putRequest
} from '../domainModelsActions';
import {Organisation, OrganisationWithoutId} from './organisationModels';
import {organisationsDataFormatter} from './organisationSchema';

export const clearOrganisationErrors = clearError(EndPoints.organisations);

export const fetchOrganisations = fetchIfNeeded<Organisation>(
  EndPoints.organisations,
  'organisations',
  organisationsDataFormatter,
);

export const fetchSubOrganisations = fetchIfNeededForSector<Organisation>(
  Sectors.subOrganisations,
  EndPoints.subOrganisations,
  'subOrganisations',
  organisationsDataFormatter,
);

export const clearSubOrganisations = emptyActionOf(domainModelsClear(Sectors.subOrganisations));

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

const createOrganisationCallbacks = {
  afterSuccess: (organisation: Organisation, dispatch: Dispatch<RootState>) => {
    dispatch(showSuccessMessage(
      firstUpperTranslated(
        'successfully created the organisation {{name}} ({{slug}})',
        {...organisation},
      ),
    ));
    dispatch(routerActions.push(`${routes.adminOrganisations}`));
  },
  afterFailure: ({message}: ErrorResponse, dispatch: Dispatch<RootState>) => {
    dispatch(showFailMessage(firstUpperTranslated(
      'failed to create organisation: {{error}}',
      {error: message},
    )));
  },
};

export const addOrganisation: CallbackWithData =
  postRequest<OrganisationWithoutId>(EndPoints.organisations, createOrganisationCallbacks);

export const addSubOrganisation: CallbackWithDataAndUrlParameters =
  postRequestToUrl<OrganisationWithoutId, uuid>(
    EndPoints.organisations,
    createOrganisationCallbacks,
    (parentId: uuid) => `${EndPoints.organisations}/${parentId}/sub-organisations`
  );

export const updateOrganisation: CallbackWithData =
  putRequest<Organisation, Organisation>(
    EndPoints.organisations,
    {
      afterSuccess: (organisation: Organisation, dispatch: Dispatch<RootState>) => {
        dispatch(showSuccessMessage(
          firstUpperTranslated(
            'successfully updated the organisation {{name}} ({{slug}})',
            {...organisation},
          ),
        ));
      },
      afterFailure: ({message}: ErrorResponse, dispatch: Dispatch<RootState>) => {
        dispatch(showFailMessage(firstUpperTranslated(
          'failed to update organisation: {{error}}',
          {error: message},
        )));
      },
    }
  );

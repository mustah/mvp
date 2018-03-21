import {Dispatch} from 'react-redux';
import {RootState} from '../../../reducers/rootReducer';
import {EndPoints} from '../../../services/endPoints';
import {firstUpperTranslated} from '../../../services/translationService';
import {ErrorResponse} from '../../../types/Types';
import {showFailMessage, showSuccessMessage} from '../../ui/message/messageActions';
import {clearError, deleteRequest, fetchIfNeeded, postRequest} from '../domainModelsActions';
import {Organisation} from './organisationModels';
import {organisationSchema} from './organisationSchema';

export const clearOrganisationErrors = clearError(EndPoints.organisations);

export const fetchOrganisations =
  fetchIfNeeded<Organisation>(EndPoints.organisations, organisationSchema, 'organisations');

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

export const addOrganisation = postRequest<Organisation>(EndPoints.organisations, {
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
});

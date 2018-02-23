import {Dispatch} from 'react-redux';
import {RootState} from '../../../reducers/rootReducer';
import {firstUpperTranslated} from '../../../services/translationService';
import {ErrorResponse} from '../../../types/Types';
import {showFailMessage, showSuccessMessage} from '../../ui/message/messageActions';
import {EndPoints} from '../domainModels';
import {clearError, restDelete, restGetIfNeeded, restPost} from '../domainModelsActions';
import {Organisation} from '../user/userModels';
import {organisationSchema} from '../user/userSchema';

export const clearOrganisationErrors = clearError(EndPoints.organisations);

export const fetchOrganisations =
  restGetIfNeeded<Organisation>(EndPoints.organisations, organisationSchema, 'organisations');

export const deleteOrganisation = restDelete<Organisation>(EndPoints.organisations, {
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
export const addOrganisation = restPost<Organisation>(EndPoints.organisations, {
  afterSuccess: (organisation: Organisation, dispatch: Dispatch<RootState>) => {
    dispatch(showSuccessMessage(
      firstUpperTranslated(
        'successfully created the organisation {{name}} ({{code}})',
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

import {Dispatch} from 'react-redux';
import {routerActions} from 'react-router-redux';
import {routes} from '../../../app/routes';
import {RootState} from '../../../reducers/rootReducer';
import {EndPoints} from '../../../services/endPoints';
import {firstUpperTranslated} from '../../../services/translationService';
import {CallbackWithData, ErrorResponse} from '../../../types/Types';
import {showFailMessage, showSuccessMessage} from '../../ui/message/messageActions';
import {clearError, deleteRequest, fetchIfNeeded, postRequest, putRequest} from '../domainModelsActions';
import {MeterDefinition, MeterDefinitionMaybeId} from './meterDefinitionModels';
import {meterDefinitionsDataFormatter} from './meterDefinitionSchema';

export const clearMeterDefinitionErrors = clearError(EndPoints.meterDefinitions);

export const fetchMeterDefinitions = fetchIfNeeded<MeterDefinition>(
  EndPoints.meterDefinitions,
  'meterDefinitions',
  meterDefinitionsDataFormatter,
);

export const deleteMeterDefinition = deleteRequest<MeterDefinition>(EndPoints.meterDefinitions, {
    afterSuccess: (meterDefinition: MeterDefinition, dispatch: Dispatch<RootState>) => {
      const translatedMessage = firstUpperTranslated(
        'successfully deleted the meter definition {{name}}',
        {...meterDefinition},
      );
      dispatch(showSuccessMessage(translatedMessage));
    },
    afterFailure: ({message}: ErrorResponse, dispatch: Dispatch<RootState>) => {
      const translatedMessage = firstUpperTranslated(
        'failed to delete the meter definition: {{error}}',
        {error: message},
      );
      dispatch(showFailMessage(translatedMessage));
    },
  },
);

const createMeterDefinitionCallbacks = {
  afterSuccess: (meterDefinition: MeterDefinition, dispatch: Dispatch<RootState>) => {
    dispatch(showSuccessMessage(
      firstUpperTranslated(
        'successfully created the meter definition {{name}}',
        {...meterDefinition},
      ),
    ));
    dispatch(routerActions.push(`${routes.adminMeterDefinitions}`));
  },
  afterFailure: ({message}: ErrorResponse, dispatch: Dispatch<RootState>) => {
    dispatch(showFailMessage(firstUpperTranslated(
      'failed to create meter definition: {{error}}',
      {error: message},
    )));
  },
};

export const addMeterDefinition: CallbackWithData =
  postRequest<MeterDefinitionMaybeId>(EndPoints.meterDefinitions, createMeterDefinitionCallbacks);

export const updateMeterDefinition: CallbackWithData =
  putRequest<MeterDefinition, MeterDefinition>(
    EndPoints.meterDefinitions,
    {
      afterSuccess: (meterDefinition: MeterDefinition, dispatch: Dispatch<RootState>) => {
        dispatch(showSuccessMessage(
          firstUpperTranslated(
            'successfully updated the meter definition {{name}}',
            {...meterDefinition},
          ),
        ));
      },
      afterFailure: ({message}: ErrorResponse, dispatch: Dispatch<RootState>) => {
        dispatch(showFailMessage(firstUpperTranslated(
          'failed to update meter definition: {{error}}',
          {error: message},
        )));
      },
    }
  );

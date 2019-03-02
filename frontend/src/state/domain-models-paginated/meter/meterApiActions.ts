import {Dispatch} from 'react-redux';
import {RootState} from '../../../reducers/rootReducer';
import {EndPoints} from '../../../services/endPoints';
import {firstUpperTranslated} from '../../../services/translationService';
import {ErrorResponse, uuid} from '../../../types/Types';
import {showFailMessage, showSuccessMessage} from '../../ui/message/messageActions';
import {updatePageMetaData} from '../../ui/pagination/paginationActions';
import {NormalizedPaginated} from '../paginatedDomainModels';
import {clearError, fetchIfNeeded, sortTableAction} from '../paginatedDomainModelsActions';
import {paginatedDeleteRequest} from '../paginatedDomainModelsEntityActions';
import {Meter} from './meterModels';
import {meterDataFormatter} from './meterSchema';

export const fetchMeters = fetchIfNeeded<Meter>(
  EndPoints.meters,
  meterDataFormatter,
  'meters',
  {
    afterSuccess: ({result}: NormalizedPaginated<Meter>, dispatch) =>
      dispatch(updatePageMetaData({entityType: 'meters', ...result})),
  },
);

export type OnDeleteMeter = (id: uuid, page: number) => void;

export const deleteMeter: OnDeleteMeter = paginatedDeleteRequest<Meter>(EndPoints.meters, {
    afterSuccess: ({facility}: Meter, dispatch: Dispatch<RootState>) => {
      const translatedMessage = firstUpperTranslated(
        'successfully deleted the meter {{facility}}',
        {facility},
      );
      dispatch(showSuccessMessage(translatedMessage));
    },
    afterFailure: ({message: error}: ErrorResponse, dispatch: Dispatch<RootState>) => {
      const translatedMessage = firstUpperTranslated(
        'failed to delete the meter: {{error}}',
        {error},
      );
      dispatch(showFailMessage(translatedMessage));
    },
  },
);

export const clearErrorMeters = clearError(EndPoints.meters);
export const sortTableMeters = sortTableAction(EndPoints.meters);

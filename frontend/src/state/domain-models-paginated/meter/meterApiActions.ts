import {getId} from '../../../helpers/collections';
import {GetState} from '../../../reducers/rootReducer';
import {EndPoints} from '../../../services/endPoints';
import {firstUpperTranslated} from '../../../services/translationService';
import {Dispatch, ErrorResponse, uuid} from '../../../types/Types';
import {syncMeters} from '../../../usecases/meter/meterActions';
import {addAllToReport} from '../../report/reportActions';
import {getLegendItemsWithLimit} from '../../report/reportSelectors';
import {showFailMessage, showSuccessMessage} from '../../ui/message/messageActions';
import {updatePageMetaData} from '../../ui/pagination/paginationActions';
import {NormalizedPaginated} from '../paginatedDomainModels';
import {clearError, fetchIfNeeded} from '../paginatedDomainModelsActions';
import {paginatedDeleteRequest} from '../paginatedDomainModelsEntityActions';
import {getAllMeters} from '../paginatedDomainModelsSelectors';
import {Meter} from './meterModels';
import {meterDataFormatter} from './meterSchema';

export const fetchMeters = fetchIfNeeded<Meter>(
  EndPoints.meters,
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
    afterSuccess: ({facility}: Meter, dispatch: Dispatch) => {
      const translatedMessage = firstUpperTranslated(
        'successfully deleted the meter {{facility}}',
        {facility},
      );
      dispatch(showSuccessMessage(translatedMessage));
    },
    afterFailure: ({message: error}: ErrorResponse, dispatch: Dispatch) => {
      const translatedMessage = firstUpperTranslated(
        'failed to delete the meter: {{error}}',
        {error},
      );
      dispatch(showFailMessage(translatedMessage));
    },
  },
);

export const clearErrorMeters = clearError(EndPoints.meters);

export const clearMetersErrorOnPage = () =>
  (dispatch, getState: GetState) => {
    const {page} = getState().ui.pagination.meters;
    dispatch(clearErrorMeters({page}));
  };

export const addMetersOnPageToReport = () =>
  (dispatch, getState: GetState) => {
    const {domainModels: {legendItems}} = getState();
    dispatch(addAllToReport(getLegendItemsWithLimit(legendItems)));
  };

export const syncMetersOnPage = () =>
  (dispatch, getState: GetState) => {
    const {paginatedDomainModels: {meters}} = getState();
    dispatch(syncMeters(getAllMeters(meters).map(getId)));
  };

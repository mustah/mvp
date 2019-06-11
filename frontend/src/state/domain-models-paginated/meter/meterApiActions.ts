import {GetState} from '../../../reducers/rootReducer';
import {EndPoints} from '../../../services/endPoints';
import {firstUpperTranslated} from '../../../services/translationService';
import {Dispatch, ErrorResponse, uuid} from '../../../types/Types';
import {syncMeters} from '../../../usecases/meter/meterActions';
import {toLegendItem} from '../../../usecases/report/helpers/legendHelper';
import {addAllToReport} from '../../report/reportActions';
import {showFailMessage, showSuccessMessage} from '../../ui/message/messageActions';
import {updatePageMetaData} from '../../ui/pagination/paginationActions';
import {NormalizedPaginated} from '../paginatedDomainModels';
import {clearError, fetchIfNeeded} from '../paginatedDomainModelsActions';
import {paginatedDeleteRequest} from '../paginatedDomainModelsEntityActions';
import {getPaginatedResult} from '../paginatedDomainModelsSelectors';
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
    const {ui: {pagination: {meters: {page}}}, paginatedDomainModels: {meters}} = getState();
    const result = getPaginatedResult(meters, page);
    dispatch(addAllToReport(result.map(id => meters.entities[id]).map(toLegendItem)));
  };

export const syncMetersOnPage = () =>
  (dispatch, getState: GetState) => {
    const {ui: {pagination: {meters: {page}}}, paginatedDomainModels: {meters}} = getState();
    dispatch(syncMeters(getPaginatedResult(meters, page)));
  };

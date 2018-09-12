import {EndPoints} from '../../../services/endPoints';
import {updatePageMetaData} from '../../ui/pagination/paginationActions';
import {NormalizedPaginated} from '../paginatedDomainModels';
import {clearError, fetchIfNeeded} from '../paginatedDomainModelsActions';
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

export const clearErrorMeters = clearError(EndPoints.meters);

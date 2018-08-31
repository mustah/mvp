import {EndPoints} from '../../../services/endPoints';
import {updatePageMetaData} from '../../ui/pagination/paginationActions';
import {NormalizedPaginated} from '../paginatedDomainModels';
import {clearError, fetchIfNeeded} from '../paginatedDomainModelsActions';
import {fetchEntitiesIfNeeded} from '../paginatedDomainModelsEntityActions';
import {Meter} from './meterModels';
import {meterDataFormatter, meterProcessStrategy} from './meterSchema';

export const fetchMeters = fetchIfNeeded<Meter>(
  EndPoints.meters,
  meterDataFormatter,
  'meters',
  {
    afterSuccess: ({result}: NormalizedPaginated<Meter>, dispatch) =>
      dispatch(updatePageMetaData({entityType: 'meters', ...result})),
  },
);

export const fetchMeterEntities = fetchEntitiesIfNeeded<Meter>(
  EndPoints.meters,
  'meters',
  (data: Meter[]) => data.map(meterProcessStrategy),
);

export const clearErrorMeters = clearError(EndPoints.meters);

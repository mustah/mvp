import {EndPoints} from '../../../services/endPoints';
import {paginationUpdateMetaData} from '../../ui/pagination/paginationActions';
import {NormalizedPaginated} from '../paginatedDomainModels';
import {clearError, fetchIfNeeded} from '../paginatedDomainModelsActions';
import {fetchEntitiesIfNeeded, fetchEntityIfNeeded} from '../paginatedDomainModelsEntityActions';
import {Meter} from './meterModels';
import {meterSchema} from './meterSchema';

export const fetchMeters = fetchIfNeeded<Meter>(EndPoints.meters, meterSchema, 'meters', {
  afterSuccess: (
    {result}: NormalizedPaginated<Meter>,
    dispatch,
  ) => dispatch(paginationUpdateMetaData({entityType: 'meters', ...result})),
});

export const fetchMeter = fetchEntityIfNeeded<Meter>(EndPoints.meters, 'meters');
export const fetchMeterEntities = fetchEntitiesIfNeeded<Meter>(EndPoints.meters, 'meters');
export const clearErrorMeters = clearError(EndPoints.meters);

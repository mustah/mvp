import {EndPoints} from '../../../services/endPoints';
import {Meter} from '../../domain-models-paginated/meter/meterModels';
import {allMetersSchema} from '../../domain-models-paginated/meter/meterSchema';
import {paginationUpdateMetaData} from '../../ui/pagination/paginationActions';
import {
  clearError, fetchIfNeeded, paginationMetaDataOf,
} from '../domainModelsActions';

export const fetchAllMeters = fetchIfNeeded<Meter>(
  EndPoints.allMeters,
  allMetersSchema,
  'allMeters',
  {
    afterSuccess: (
      {result},
      dispatch,
    ) => dispatch(paginationUpdateMetaData({
      entityType: 'allMeters', ...paginationMetaDataOf(result),
    })),
  },
);

export const clearErrorAllMeters = clearError(EndPoints.allMeters);

import {Meter} from '../../domain-models-paginated/meter/meterModels';
import {allMetersSchema} from '../../domain-models-paginated/meter/meterSchema';
import {paginationUpdateMetaData} from '../../ui/pagination/paginationActions';
import {EndPoints} from '../domainModels';
import {clearError, paginationMetaDataOf, fetchIfNeeded} from '../domainModelsActions';

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

import {paginationUpdateMetaData} from '../../ui/pagination/paginationActions';
import {EndPoints} from '../domainModels';
import {clearError, paginationMetaDataOf, fetchIfNeeded} from '../domainModelsActions';
import {Gateway} from './gatewayModels';
import {gatewaySchema} from './gatewaySchema';

export const clearErrorGateways = clearError(EndPoints.gateways);
export const fetchGateways = fetchIfNeeded<Gateway>(
  EndPoints.gateways,
  gatewaySchema,
  'gateways',
  {
    afterSuccess: (
      {result},
      dispatch,
    ) => dispatch(paginationUpdateMetaData({
      entityType: 'gateways', ...paginationMetaDataOf(result),
    })),
  },
);

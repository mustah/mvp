import {paginationUpdateMetaData} from '../../ui/pagination/paginationActions';
import {EndPoints} from '../domainModels';
import {clearError, paginationMetaDataFromResult, restGetIfNeeded} from '../domainModelsActions';
import {Gateway} from './gatewayModels';
import {gatewaySchema} from './gatewaySchema';

export const clearErrorGateways = clearError(EndPoints.gateways);
export const fetchGateways = restGetIfNeeded<Gateway>(
  EndPoints.gateways,
  gatewaySchema,
  'gateways',
  {
    afterSuccess: (
      {result},
      dispatch,
    ) => dispatch(paginationUpdateMetaData({
      entityType: 'gateways', ...paginationMetaDataFromResult(result),
    })),
  },
);

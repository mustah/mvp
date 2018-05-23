import {EndPoints} from '../../../services/endPoints';
import {paginationUpdateMetaData} from '../../ui/pagination/paginationActions';
import {NormalizedPaginated} from '../paginatedDomainModels';
import {clearError, fetchIfNeeded} from '../paginatedDomainModelsActions';
import {fetchEntityIfNeeded} from '../paginatedDomainModelsEntityActions';
import {Gateway} from './gatewayModels';
import {gatewaySchema} from './gatewaySchema';

export const clearErrorGateways = clearError(EndPoints.gateways);

export const fetchGateways = fetchIfNeeded<Gateway>(
  EndPoints.gateways,
  gatewaySchema,
  'gateways',
  {
    afterSuccess: (
      {result}: NormalizedPaginated<Gateway>,
      dispatch,
    ) => dispatch(paginationUpdateMetaData({entityType: 'gateways', ...result})),
  },
);

export const fetchGateway = fetchEntityIfNeeded(EndPoints.gateways, 'gateways');

import {EndPoints} from '../../../services/endPoints';
import {updatePageMetaData} from '../../ui/pagination/paginationActions';
import {NormalizedPaginated} from '../paginatedDomainModels';
import {clearError, fetchIfNeeded} from '../paginatedDomainModelsActions';
import {fetchEntityIfNeeded} from '../paginatedDomainModelsEntityActions';
import {Gateway} from './gatewayModels';
import {gatewayDataFormatter} from './gatewaySchema';

export const clearErrorGateways = clearError(EndPoints.gateways);

export const fetchGateways = fetchIfNeeded<Gateway>(
  EndPoints.gateways,
  EndPoints.gateways,
  gatewayDataFormatter,
  'gateways',
  {
    afterSuccess: (
      {result}: NormalizedPaginated<Gateway>,
      dispatch,
    ) => dispatch(updatePageMetaData({entityType: 'gateways', ...result})),
  },
);

export const fetchGateway = fetchEntityIfNeeded(EndPoints.gateways, 'gateways');

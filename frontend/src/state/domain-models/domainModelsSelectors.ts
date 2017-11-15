import {uuid} from '../../types/Types';
import {NormalizedState, SelectionEntity, SelectionEntityState} from './domainModels';
import {GatewaysState} from './gateway/gatewayModels';
import {MetersState} from './meter/meterModels';

// TODO: Fix typing of this.
export const getResultDomainModels = (state: NormalizedState<SelectionEntity> | MetersState | GatewaysState): uuid[] =>
  state.result;

export const isFetchingDomainModels = (state: SelectionEntityState): boolean => state.isFetching;

import {uuid} from '../../types/Types';
import {NormalizedState, SelectionEntity} from './domainModels';
import {GatewaysState} from './gateway/gatewayModels';
import {MetersState} from './meter/meterModels';

// TODO: Fix typing of this.
export const getResultDomainModels = (state: NormalizedState<SelectionEntity> | MetersState | GatewaysState): uuid[] =>
  state.result;

import {uuid} from '../../types/Types';
import {NormalizedState, SelectionEntity} from './domainModels';
import {MetersState} from './meter/meterModels';
import {GatewaysState} from './gateway/gatewayModels';

// TODO: Fix typing of this.
export const getResultDomainModels = (state: NormalizedState<SelectionEntity> | MetersState | GatewaysState): uuid[] =>
  state.result;

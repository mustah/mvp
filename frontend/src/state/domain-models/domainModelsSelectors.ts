import {uuid} from '../../types/Types';
import {SelectionEntityState} from './domainModels';
import {GatewaysState} from './gateway/gatewayModels';
import {MetersState} from './meter/meterModels';

type State = MetersState | GatewaysState | SelectionEntityState;

export const getResultDomainModels = (state: State): uuid[] =>
  state.result;

import {uuid} from '../../types/Types';
import {SelectionEntityState} from './domainModels';
import {GatewaysState} from './gateway/gatewayModels';
import {MetersState} from './meter/meterModels';
import {UserState} from './user/userModels';

type State = MetersState | GatewaysState | SelectionEntityState | UserState;

export const getResultDomainModels = (state: State): uuid[] =>
  state.result;

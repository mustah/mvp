import {uuid} from '../../types/Types';
import {SelectionEntityState} from './domainModels';
import {GatewaysState} from './gateway/gatewayModels';
import {UserState} from './user/userModels';

type State = GatewaysState | SelectionEntityState | UserState;

export const getResultDomainModels = (state: State): uuid[] =>
  state.result;

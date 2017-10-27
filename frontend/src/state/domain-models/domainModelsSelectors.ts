import {GatewaysState} from './gateway/gatewayModels';
import {MetersState} from './meter/meterModels';
import {uuid} from '../../types/Types';

export type DomainModel = GatewaysState | MetersState;
export const getResult = (state: DomainModel): uuid[] => state.result;

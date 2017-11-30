import {DomainModel} from '../domainModels';
import {Gateway, GatewaysState} from './gatewayModels';

export const getGatewaysTotal = (state: GatewaysState): number => state.total;
export const getGatewayEntities = (state: GatewaysState): DomainModel<Gateway> => state.entities;

import {Gateway, GatewaysState} from './gatewayModels';

// TODO: Perhaps move this to domainSelectors
export const getGatewaysTotal = (state: GatewaysState): number => state.total;
export const getGatewayEntities = (state: GatewaysState): {[key: string]: Gateway} => state.entities.gateways;

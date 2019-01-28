import {Identifiable, IdNamed, uuid} from '../../../types/Types';
import {LocationHolder} from '../../domain-models/location/locationModels';
import {NormalizedPaginatedState} from '../paginatedDomainModels';

export interface GatewayStatusChangelog extends Identifiable {
  gatewayId: uuid;
  status: IdNamed;
  date: string;
}

export interface GatewayMandatory extends Identifiable {
  serial: string;
  productModel: string;
  status: IdNamed;
  ip: string;
  phoneNumber: string;
}

export interface Gateway extends LocationHolder, GatewayMandatory {
  statusChanged?: string;
  signalToNoiseRatio?: number;
  statusChangelog: GatewayStatusChangelog[];
  meterIds: uuid[];
  organisationId: uuid;
}

export type GatewaysState = NormalizedPaginatedState<Gateway>;

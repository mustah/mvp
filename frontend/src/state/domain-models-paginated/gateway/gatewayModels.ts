import {PieData} from '../../../components/pie-chart-selector/PieChartSelector';
import {Identifiable, IdNamed, uuid} from '../../../types/Types';
import {Flag} from '../../domain-models/flag/flagModels';
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
}

export interface Gateway extends LocationHolder, GatewayMandatory {
  flags: Flag[];
  flagged: boolean;
  statusChanged?: string;
  signalToNoiseRatio?: number;
  statusChangelog: GatewayStatusChangelog[];
  meterIds: uuid[];
}

export type GatewaysState = NormalizedPaginatedState<Gateway>;

export interface GatewayDataSummary {
  status: PieData;
  flagged: PieData;
  location: PieData;
  productModel: PieData;
}

export type GatewayDataSummaryKey = keyof GatewayDataSummary;

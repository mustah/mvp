import {PieData} from '../../../components/pie-chart-selector/PieChartSelector';
import {Identifiable, IdNamed, uuid} from '../../../types/Types';
import {NormalizedState} from '../domainModels';
import {Flag} from '../flag/flagModels';
import {LocationHolder} from '../location/locationModels';

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
  meterStatus: IdNamed;
  meterAlarm: string;
  meterManufacturer: string;
}

export type GatewaysState = NormalizedState<Gateway>;

export interface GatewayDataSummary {
  status: PieData;
  flagged: PieData;
  location: PieData;
  productModel: PieData;
}

export type GatewayDataSummaryKey = keyof GatewayDataSummary;

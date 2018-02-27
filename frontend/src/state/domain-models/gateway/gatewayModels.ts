import {PieData} from '../../../components/pie-chart-selector/PieChartSelector';
import {IdNamed, uuid} from '../../../types/Types';
import {Location, NormalizedState} from '../domainModels';
import {Flag} from '../flag/flagModels';

export interface GatewayStatusChangelog {
  id: uuid;
  gatewayId: uuid;
  status: IdNamed;
  date: string;
}

export interface Gateway extends Location {
  id: uuid;
  facility: string;
  flags: Flag[];
  flagged: boolean;
  productModel: string;
  statusChanged?: string;
  signalToNoiseRatio?: number;
  status: IdNamed;
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
  city: PieData;
  productModel: PieData;
}

export type GatewayDataSummaryKey = keyof GatewayDataSummary;

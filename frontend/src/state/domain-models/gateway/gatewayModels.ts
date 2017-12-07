import {IdNamed, Maybe, uuid} from '../../../types/Types';
import {NormalizedState, Location} from '../domainModels';
import {Flag} from '../flag/flagModels';
import {PieData} from '../../../components/pie-chart-selector/PieChartSelector';

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
  telephoneNumber: string;
  statusChanged?: string;
  ip?: Maybe<string>;
  port?: Maybe<string>;
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

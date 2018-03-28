import {PieData} from '../../../components/pie-chart-selector/PieChartSelector';
import {Identifiable, IdNamed, uuid} from '../../../types/Types';
import {Flag} from '../../domain-models/flag/flagModels';
import {LocationHolder} from '../../domain-models/location/locationModels';
import {GatewayMandatory} from '../gateway/gatewayModels';
import {NormalizedPaginatedState} from '../paginatedDomainModels';

export interface MeterStatusChangelog extends Identifiable {
  name: string;
  start: string;
}

export type MetersState = NormalizedPaginatedState<Meter>;

export interface Meter extends Identifiable, LocationHolder {
  sapId?: uuid;
  measurementId?: uuid;
  facility: uuid;
  alarm: string;
  flags: Flag[];
  flagged: boolean;
  medium: string;
  manufacturer: string;
  statusChanged?: string;
  statusChangelog: MeterStatusChangelog[];
  date?: string;
  status: IdNamed;
  gateway: GatewayMandatory;
}

export interface MeterDataSummary {
  flagged: PieData;
  location: PieData;
  manufacturer: PieData;
  medium: PieData;
  status: PieData;
  alarm: PieData;
}

export type MeterDataSummaryKey = keyof MeterDataSummary;

import {PieData} from '../../../components/pie-chart-selector/PieChartSelector';
import {Identifiable, IdNamed, uuid} from '../../../types/Types';
import {LocationHolder} from '../../domain-models/location/locationModels';
import {NormalizedPaginatedState} from '../paginatedDomainModels';

export interface MeterStatusChangelog extends Identifiable {
  name: string;
  start: string;
}

export type MetersState = NormalizedPaginatedState<Meter>;

export interface Meter extends Identifiable, LocationHolder {
  address?: string;
  collectionPercentage?: number;
  readIntervalMinutes?: number;
  facility: uuid;
  medium: string;
  manufacturer: string;
  statusChanged?: string;
  status: IdNamed;
  gatewaySerial: string;
  organisationId: uuid;
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

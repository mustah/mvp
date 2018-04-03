import {PieData} from '../../../components/pie-chart-selector/PieChartSelector';
import {Identifiable, IdNamed, uuid} from '../../../types/Types';
import {Flag} from '../../domain-models/flag/flagModels';
import {LocationHolder} from '../../domain-models/location/locationModels';
import {Measurement} from '../../ui/graph/measurement/measurementModels';
import {GatewayMandatory} from '../gateway/gatewayModels';
import {NormalizedPaginatedState} from '../paginatedDomainModels';

export interface MeterStatusChangelog extends Identifiable {
  name: string;
  start: string;
}

export type MetersState = NormalizedPaginatedState<Meter>;

export interface Meter extends Identifiable, LocationHolder {
  sapId?: uuid;
  created: string;
  collectionStatus: string;
  readIntervalMinutes: number;
  facility: uuid;
  alarm: string;
  flags: Flag[];
  flagged: boolean;
  medium: string;
  manufacturer: string;
  measurements: Measurement[];
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

export const enum RenderableQuantity {
  volume = 'Volume',
  flow = 'Flow',
  energy = 'Energy',
  power = 'Power',
  forwardTemperature = 'Forward temperature',
  returnTemperature = 'Return temperature',
  differenceTemperature = 'Difference temperature',
}

export const allQuantities = {
  heat: [
    RenderableQuantity.volume,
    RenderableQuantity.flow,
    RenderableQuantity.energy,
    RenderableQuantity.power,
    RenderableQuantity.forwardTemperature,
    RenderableQuantity.returnTemperature,
    RenderableQuantity.differenceTemperature,
  ],
};

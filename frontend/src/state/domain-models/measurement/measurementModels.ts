import {uuid} from '../../../types/Types';
import {NormalizedPaginatedState} from '../paginatedDomainModels';

interface PhysicalMeter {
  rel: string;
  href: string;
}

export interface Measurement {
  id: uuid;
  created: number;
  value: number;
  quantity: string;
  unit: string;
  physicalMeter: PhysicalMeter;
}

export type MeasurementState = NormalizedPaginatedState<Measurement>;

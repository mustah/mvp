import {Identifiable} from '../../../types/Types';
import {NormalizedState} from '../domainModels';
import {Organisation} from '../organisation/organisationModels';

interface PhysicalMeter extends Identifiable {
  organisation: Organisation;
  identity: string;
  medium: string;
}

export interface Measurement extends Identifiable {
  created: number;
  value: number;
  quantity: string;
  unit: string;
  physicalMeter: PhysicalMeter;
}

export type MeasurementState = NormalizedState<Measurement>;

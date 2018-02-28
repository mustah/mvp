import {HasId} from '../../../types/Types';
import {NormalizedState} from '../domainModels';
import {Organisation} from '../organisation/organisationModels';

interface PhysicalMeter extends HasId {
  organisation: Organisation;
  identity: string;
  medium: string;
}

export interface Measurement extends HasId {
  created: number;
  value: number;
  quantity: string;
  unit: string;
  physicalMeter: PhysicalMeter;
}

export type MeasurementState = NormalizedState<Measurement>;

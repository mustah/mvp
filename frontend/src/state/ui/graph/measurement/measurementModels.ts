import {Identifiable} from '../../../../types/Types';
import {Organisation} from '../../../domain-models/organisation/organisationModels';

interface PhysicalMeter extends Identifiable {
  organisation: Organisation;
  identity: string;
  medium: string;
}

// TODO there's no physicalMeter any longer, remove it.. where is this interface used really?
export interface Measurement extends Identifiable {
  created: number;
  value: number;
  quantity: string;
  unit: string;
  physicalMeter: PhysicalMeter;
}

export type Quantity = string;

export interface MeasurementApiResponsePart {
  quantity: string;
  unit: string;
  label: string;
  values: Array<{
    when: number;
    value: number;
  }>;
}

export interface AverageApiResponsePart {
  quantity: string;
  unit: string;
  values: Array<{
    when: number;
    value: number;
  }>;
}

export type AverageApiResponse = AverageApiResponsePart[];

export type MeasurementApiResponse = MeasurementApiResponsePart[];

export interface MeasurementResponses {
  measurement: MeasurementApiResponse;
  average: AverageApiResponse;
}

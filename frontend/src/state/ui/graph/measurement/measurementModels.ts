import {Identifiable} from '../../../../types/Types';
import {GraphContents} from '../../../../usecases/report/reportModels';
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
  quantity: Quantity;
  unit: string;
  physicalMeter: PhysicalMeter;
}

export type Quantity = string;

export const emptyGraphContents: GraphContents = {
  axes: {},
  data: [],
  legend: [],
  lines: [],
};

export interface MeasurementApiResponsePart {
  quantity: Quantity;
  unit: string;
  label: string;
  values: Array<{
    when: number;
    value: number;
  }>;
}

export type MeasurementApiResponse = MeasurementApiResponsePart[];

export interface MeasurementResponses {
  measurement: MeasurementApiResponse;
  average: MeasurementApiResponse;
}

export interface MeasurementState {
  selectedQuantities: Quantity[];
}

export const enum RenderableQuantity {
  volume = 'Volume',
  flow = 'Flow',
  energy = 'Energy',
  power = 'Power',
  forwardTemperature = 'Forward temperature',
  returnTemperature = 'Return temperature',
  differenceTemperature = 'Difference temperature',
}

export const enum Resolution {
  hour = 'hour',
  day = 'day',
  month = 'month',
}

export const allQuantities = {
  heat: [
    RenderableQuantity.energy,
    RenderableQuantity.volume,
    RenderableQuantity.power,
    RenderableQuantity.flow,
    RenderableQuantity.forwardTemperature,
    RenderableQuantity.returnTemperature,
    RenderableQuantity.differenceTemperature,
  ],
  gas: [
    RenderableQuantity.volume,
  ],
};

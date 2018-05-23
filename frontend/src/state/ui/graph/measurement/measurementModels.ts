import {Medium} from '../../../../components/indicators/indicatorWidgetModels';
import {Maybe} from '../../../../helpers/Maybe';
import {Identifiable} from '../../../../types/Types';
import {GraphContainerState} from '../../../../usecases/report/containers/GraphContainer';
import {GraphContents} from '../../../../usecases/report/reportModels';

export interface Measurement extends Identifiable {
  created: number;
  value: number;
  quantity: Quantity;
  unit: string;
}

export type Quantity = string;

const emptyGraphContents: GraphContents = {
  axes: {},
  data: [],
  legend: [],
  lines: [],
};

export const initialState: GraphContainerState = {
  hiddenKeys: [],
  graphContents: emptyGraphContents,
  isFetching: false,
  error: Maybe.nothing(),
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

export const allQuantities = {
  [Medium.districtHeating]: [
    RenderableQuantity.energy,
    RenderableQuantity.volume,
    RenderableQuantity.power,
    RenderableQuantity.flow,
    RenderableQuantity.forwardTemperature,
    RenderableQuantity.returnTemperature,
    RenderableQuantity.differenceTemperature,
  ],
  [Medium.gas]: [
    RenderableQuantity.volume,
  ],
};

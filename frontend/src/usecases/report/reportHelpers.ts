import {GraphContents} from './reportModels';

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

export const emptyGraphContents: GraphContents = {
  axes: {},
  data: [],
  legend: [],
  lines: [],
};

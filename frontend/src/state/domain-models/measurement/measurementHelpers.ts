import {Measurement} from './measurementModels';

// TODO get label/id of meter so that we can render something pretty here
export const labelOfMeasurement = (entity: Measurement): string =>
  entity.physicalMeter.href.split('/').pop() as string;

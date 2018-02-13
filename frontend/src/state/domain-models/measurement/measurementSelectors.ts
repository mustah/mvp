import {ObjectsById} from '../domainModels';
import {Measurement, MeasurementState} from './measurementModels';

export const getMeasurements = (state: MeasurementState): ObjectsById<Measurement> =>
  state.entities;

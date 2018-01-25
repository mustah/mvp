import {DomainModel} from '../domainModels';
import {Measurement, MeasurementState} from './measurementModels';

export const getMeasurements = (state: MeasurementState): DomainModel<Measurement> =>
  state.entities;

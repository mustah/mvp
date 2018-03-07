import {EndPoints} from '../domainModels';
import {fetchIfNeeded} from '../domainModelsActions';
import {Measurement} from './measurementModels';
import {measurementSchema} from './measurementSchema';

export const fetchMeasurements =
  fetchIfNeeded<Measurement>(EndPoints.measurements, measurementSchema, 'measurements');

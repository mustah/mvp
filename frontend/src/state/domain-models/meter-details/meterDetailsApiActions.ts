import {EndPoints} from '../../../services/endPoints';
import {fetchEntityIfNeeded} from '../domainModelsActions';
import {MeterDetails} from './meterDetailsModels';

export const fetchMeter = fetchEntityIfNeeded<MeterDetails>(
  EndPoints.meters,
  'meters'
);

import {MeterDetails} from './meterDetailsModels';
import {EndPoints} from '../../../services/endPoints';
import {fetchEntityIfNeeded} from '../domainModelsActions';

export const fetchMeterDetails = fetchEntityIfNeeded<MeterDetails>(
  EndPoints.meters,
  'meters',
);

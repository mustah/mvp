import {EndPoints} from '../../../services/endPoints';
import {fetchEntityIfNeeded} from '../domainModelsActions';

export const fetchMeterDetails = fetchEntityIfNeeded(
  EndPoints.meters,
  'meters',
);

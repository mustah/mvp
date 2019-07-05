import {EndPoints} from '../../../services/endPoints';
import {fetchIfNeeded} from '../domainModelsActions';
import {Medium} from '../meter-definitions/meterDefinitionModels';
import {mediumsDataFormatter} from './mediumSchema';

export const fetchMediums = fetchIfNeeded<Medium>(
  EndPoints.mediums,
  'mediums',
  mediumsDataFormatter,
);

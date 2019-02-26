import {EndPoints} from '../../../services/endPoints';
import {
  clearError,
  fetchEntityIfNeeded,
  fetchIfNeeded,
} from '../domainModelsActions';
import {Medium} from '../meter-definitions/meterDefinitionModels';
import {mediumsDataFormatter} from './mediumSchema';

export const clearMediumsErrors = clearError(EndPoints.mediums);

export const fetchMediums = fetchIfNeeded<Medium>(
  EndPoints.mediums,
  'mediums',
  mediumsDataFormatter,
);

export const fetchMedium = fetchEntityIfNeeded(EndPoints.mediums, 'mediums');

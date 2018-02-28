import {IdNamed} from '../../../types/Types';
import {EndPoints} from '../domainModels';
import {clearError, restGetIfNeeded} from '../domainModelsActions';
import {selectionsSchema} from '../domainModelsSchemas';

// TODO: Since 'selections' isn't part of the DomainModelsState 'cities' is selected to check if anything
// have been fetched from 'selections', should perhaps come up with a better way of doing this.
export const fetchSelections = restGetIfNeeded<IdNamed>(
  EndPoints.selections,
  selectionsSchema,
  'cities',
);

export const clearErrorSelections = clearError(EndPoints.selections);

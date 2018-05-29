import {EndPoints} from '../../../services/endPoints';
import {IdNamed} from '../../../types/Types';
import {clearError, fetchIfNeeded} from '../domainModelsActions';
import {selectionsDataFormatter} from './selectionsSchemas';

// TODO: Since 'selections' isn't part of the DomainModelsState 'cities' is selected to check if
// anything have been fetched from 'selections', should perhaps come up with a better way of doing
// this.
export const fetchSelections = fetchIfNeeded<IdNamed>(
  EndPoints.selections,
  'cities',
  selectionsDataFormatter,
);

export const clearErrorSelections = clearError(EndPoints.selections);

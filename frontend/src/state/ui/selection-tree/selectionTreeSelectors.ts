import {createSelector} from 'reselect';
import {uuid} from '../../../types/Types';
import {SelectionTreeUiState} from './selectionTreeModels';

const getOpenItems = (state: SelectionTreeUiState): uuid[] => state.openListItems;

export const getOpenListItems = createSelector<SelectionTreeUiState, uuid[], Set<uuid>>(
  getOpenItems,
  (openItems: uuid[]) => new Set<uuid>(openItems),
);

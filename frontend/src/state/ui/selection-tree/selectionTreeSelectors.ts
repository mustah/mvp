import {createSelector} from 'reselect';
import {uuid} from '../../../types/Types';
import {SelectionTreeState} from './selectionTreeModels';

const getOpenItems = (state: SelectionTreeState): uuid[] => state.openListItems;

export const getOpenListItems = createSelector<SelectionTreeState, uuid[], Set<uuid>>(
  getOpenItems,
  (openItems: uuid[]) => new Set<uuid>(openItems),
);

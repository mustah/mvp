import {SelectionTreeState} from './selectionTreeReducer';
import {uuid} from '../../../types/Types';
import {createSelector} from 'reselect';

const getOpenItems = (state: SelectionTreeState): uuid[] => state.openListItems;

export const getOpenListItems = createSelector<SelectionTreeState, uuid[], Set<uuid>>(
  getOpenItems,
  (openItems: uuid[]) => new Set<uuid>(openItems),
);

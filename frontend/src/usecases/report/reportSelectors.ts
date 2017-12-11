import {uuid} from '../../types/Types';
import {ReportState} from './reportModels';
import {createSelector} from 'reselect';

const getSelectedItems = (state: ReportState): uuid[] => state.selectedListItems;

export const getSelectedListItems = createSelector<ReportState, uuid[], Set<uuid>>(
  getSelectedItems,
  (selectedItems: uuid[]) => new Set<uuid>(selectedItems),
);

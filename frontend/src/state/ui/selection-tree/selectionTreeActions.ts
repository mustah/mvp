import {createStandardAction} from 'typesafe-actions';
import {toggle} from '../../../helpers/collections';
import {GetState} from '../../../reducers/rootReducer';
import {uuid} from '../../../types/Types';

export const selectedIds = createStandardAction('SELECTION_TREE_TOGGLE_ENTRY')<uuid[]>();

export const toggleExpanded = (id: uuid) =>
  (dispatch, getState: GetState): void =>
    dispatch(selectedIds(toggle(id, getState().ui.selectionTree.openListItems)));

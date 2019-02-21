import {toggle} from '../../../helpers/collections';
import {GetState} from '../../../reducers/rootReducer';
import {payloadActionOf, uuid} from '../../../types/Types';

export const SELECTION_TREE_TOGGLE_ENTRY = 'SELECTION_TREE_TOGGLE_ENTRY';

export const selectedIds = payloadActionOf<uuid[]>(SELECTION_TREE_TOGGLE_ENTRY);

export const toggleExpanded = (id: uuid) =>
  (dispatch, getState: GetState): void =>
    dispatch(selectedIds(toggle(id, getState().ui.selectionTree.openListItems)));

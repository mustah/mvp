import {createPayloadAction} from 'react-redux-typescript';
import {toggle} from '../../../helpers/collections';
import {GetState} from '../../../reducers/rootReducer';
import {uuid} from '../../../types/Types';

export const SELECTION_TREE_TOGGLE_ENTRY = 'SELECTION_TREE_TOGGLE_ENTRY';

export const selectedIds = createPayloadAction<string, uuid[]>(SELECTION_TREE_TOGGLE_ENTRY);

export const selectionTreeToggleId = (id: uuid) =>
  (dispatch, getState: GetState): void =>
    dispatch(selectedIds(toggle(id, getState().ui.selectionTree.openListItems)));

import {createPayloadAction} from 'react-redux-typescript';
import {uuid} from '../../../types/Types';
import {RootState} from '../../../reducers/rootReducer';

export const SELECTION_TREE_TOGGLE_ENTRY = 'SELECTION_TREE_TOGGLE_ENTRY';

const toggleEntry = createPayloadAction<string, uuid[]>(SELECTION_TREE_TOGGLE_ENTRY);

const filterOutId = (selected: uuid[], id: uuid): uuid[] => selected.filter(sel => sel !== id);

export const selectionTreeToggleEntry = (id: uuid) => (dispatch, getState: () => RootState): void => {
  const {openListItems} = getState().ui.selectionTree;
  const idRemovedFromOpenListItems = filterOutId(openListItems, id);
  const idWasRemoved = openListItems.length > idRemovedFromOpenListItems.length;
  idWasRemoved ? toggleEntry(idRemovedFromOpenListItems) : toggleEntry([...openListItems, id]);
};

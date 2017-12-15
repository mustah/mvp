import {createPayloadAction} from 'react-redux-typescript';
import {RootState} from '../../../reducers/rootReducer';
import {uuid} from '../../../types/Types';

export const SELECTION_TREE_TOGGLE_ENTRY = 'SELECTION_TREE_TOGGLE_ENTRY';

const toggleEntry = createPayloadAction<string, uuid[]>(SELECTION_TREE_TOGGLE_ENTRY);

const filterOutId = (selected: uuid[], id: uuid): uuid[] => selected.filter(sel => sel !== id);

export const selectionTreeExpandToggle = (id: uuid) =>
  (dispatch, getState: () => RootState): void => {
    const {openListItems} = getState().ui.selectionTree;
    const popIdFromList = filterOutId(openListItems, id);
    const idWasRemoved = openListItems.length > popIdFromList.length;
    idWasRemoved
      ? dispatch(toggleEntry(popIdFromList))
      : dispatch(toggleEntry([...openListItems, id]));
  };

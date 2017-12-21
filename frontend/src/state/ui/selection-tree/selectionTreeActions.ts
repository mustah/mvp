import {createPayloadAction} from 'react-redux-typescript';
import {RootState} from '../../../reducers/rootReducer';
import {uuid} from '../../../types/Types';

export const SELECTION_TREE_TOGGLE_ENTRY = 'SELECTION_TREE_TOGGLE_ENTRY';

export const selectedIds = createPayloadAction<string, uuid[]>(SELECTION_TREE_TOGGLE_ENTRY);

// TODO: Using this filter method to determine if an id already exist in list,
// TODO: instead of converting list to Set as in reportActions.
const filterOutId = (selected: uuid[], id: uuid): uuid[] => selected.filter((sel) => sel !== id);

export const selectionTreeToggleId = (id: uuid) =>
  (dispatch, getState: () => RootState): void => {
    const {openListItems} = getState().ui.selectionTree;
    const popIdFromList = filterOutId(openListItems, id);
    const idWasRemoved = openListItems.length > popIdFromList.length;
    idWasRemoved
      ? dispatch(selectedIds(popIdFromList))
      : dispatch(selectedIds([...openListItems, id]));
  };

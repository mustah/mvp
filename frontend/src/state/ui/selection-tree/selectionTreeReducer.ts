import {AnyAction} from 'redux';
import {uuid} from '../../../types/Types';
import {SELECTION_TREE_TOGGLE_ENTRY} from './selectionTreeActions';

export interface SelectionTreeState {
  openListItems: uuid[];
  listItemsWithSelections: Array<{id: uuid, addedBy: uuid}>;
}

const initialState: SelectionTreeState = {
  openListItems: [],
  listItemsWithSelections: [],
};

export const selectionTree = (state: SelectionTreeState = initialState, action: AnyAction): SelectionTreeState => {
  const {payload} = action;
  switch (action.type) {
    case SELECTION_TREE_TOGGLE_ENTRY:
      return {
        ...state,
        openListItems: payload,
      };
    default:
      return state;
  }
};

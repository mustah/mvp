import {AnyAction} from 'redux';
import {SELECTION_TREE_TOGGLE_ENTRY} from './selectionTreeActions';
import {SelectionTreeState} from './selectionTreeModels';

const initialState: SelectionTreeState = {
  openListItems: [],
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

import {EmptyAction} from 'typesafe-actions/dist/types';
import {Action, uuid} from '../../../types/Types';
import {SELECTION_TREE_TOGGLE_ENTRY} from './selectionTreeActions';
import {SelectionTreeUiState} from './selectionTreeModels';

const initialState: SelectionTreeUiState = {
  openListItems: [],
};

type ActionTypes = Action<uuid[]> | EmptyAction<string>;

export const selectionTree = (
  state: SelectionTreeUiState = initialState,
  action: ActionTypes,
): SelectionTreeUiState => {
  switch (action.type) {
    case SELECTION_TREE_TOGGLE_ENTRY:
      return {
        ...state,
        openListItems: (action as Action<uuid[]>).payload,
      };
    default:
      return state;
  }
};

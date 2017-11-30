import {EmptyAction} from 'react-redux-typescript';
import {Action, uuid} from '../../../types/Types';
import {SELECTION_TREE_TOGGLE_ENTRY} from './selectionTreeActions';
import {SelectionTreeState} from './selectionTreeModels';

const initialState: SelectionTreeState = {
  openListItems: [],
};

type ActionTypes = Action<uuid[]> & EmptyAction<string>;

export const selectionTree = (state: SelectionTreeState = initialState, action: ActionTypes): SelectionTreeState => {
  switch (action.type) {
    case SELECTION_TREE_TOGGLE_ENTRY:
      return {
        ...state,
        openListItems: action.payload,
      };
    default:
      return state;
  }
};

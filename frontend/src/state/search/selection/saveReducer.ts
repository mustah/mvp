import {EmptyAction} from 'react-redux-typescript';
import {Action} from '../../../types/Types';
import {SAVE_SELECTION, UPDATE_SELECTION} from './selectionActions';
import {SelectionState} from './selectionModels';

const updateSelectionById = (state: SelectionState[] = [], {payload}: Action<SelectionState>): SelectionState[] => {
  const index = state.findIndex((selection: SelectionState) => selection.id === payload.id);
  if (index !== -1) {
    state[index] = {...payload};
    return [...state];
  } else {
    return state;
  }
};

type ActionTypes = EmptyAction<string> & Action<SelectionState>;

export const saved = (state: SelectionState[] = [], action: ActionTypes): SelectionState[] => {
  switch (action.type) {
    case SAVE_SELECTION:
      const payload: SelectionState = action.payload;
      return [payload, ...state];
    case UPDATE_SELECTION:
      return updateSelectionById(state, action);
    default:
      return state;
  }
};

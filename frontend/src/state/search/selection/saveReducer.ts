import {AnyAction} from 'redux';
import {SAVE_SELECTION, UPDATE_SELECTION} from './selectionActions';
import {SelectionState} from './selectionModels';

const updateSelectionById = (state: SelectionState[] = [], {payload}: AnyAction): SelectionState[] => {
  const index = state.findIndex((selection: SelectionState) => selection.id === payload.id);
  if (index !== -1) {
    state[index] = {...payload};
    return [...state];
  } else {
    return state;
  }
};

export const saved = (state: SelectionState[] = [], action: AnyAction): SelectionState[] => {
  const {payload, type} = action;

  switch (type) {
    case SAVE_SELECTION:
      return [payload, ...state];
    case UPDATE_SELECTION:
      return updateSelectionById(state, action);
    default:
      return state;
  }
};

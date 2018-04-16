import {combineReducers} from 'redux';
import {EmptyAction} from 'ts-redux-actions';
import {Action} from '../../types/Types';
import {SearchParameterState} from './searchParameterModels';
import {SET_CURRENT_SELECTION} from './selection/selectionActions';
import {UserSelection} from './selection/selectionModels';
import {selection} from './selection/selectionReducer';

const updateSelectionById = (state: UserSelection[], {payload}: Action<UserSelection>): UserSelection[] => {
  const index = state.findIndex((selection: UserSelection) => selection.id === payload.id);
  if (index !== -1) {
    state[index] = {...payload};
    return [...state];
  } else {
    return state;
  }
};

type ActionTypes = EmptyAction<string> & Action<UserSelection>;

export const saved = (state: UserSelection[] = [], action: ActionTypes): UserSelection[] => {
  switch (action.type) {
    case SET_CURRENT_SELECTION:
      return updateSelectionById(state, action);
    default:
      return state;
  }
};

export const searchParameters = combineReducers<SearchParameterState>({selection, saved});

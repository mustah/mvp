import {EmptyAction} from 'react-redux-typescript';
import {Period} from '../../../components/dates/dateModels';
import {Action, uuid} from '../../../types/Types';
import {
  ADD_SELECTION,
  CLOSE_SELECTION_PAGE,
  DESELECT_SELECTION,
  RESET_SELECTION,
  SAVE_SELECTION,
  SELECT_PERIOD,
  SELECT_SAVED_SELECTION,
  SET_SELECTION,
  UPDATE_SELECTION,
} from './selectionActions';
import {FilterParam, SelectionParameter, SelectionState} from './selectionModels';

export const initialState: SelectionState = {
  id: -1,
  name: 'all',
  isChanged: false,
  selected: {
    cities: [],
    addresses: [],
    meterStatuses: [],
    gatewayStatuses: [],
    alarms: [],
    manufacturers: [],
    productModels: [],
    period: Period.latest,
  },
};

const addSelected = (state: SelectionState, {payload: {parameter, id}}: Action<SelectionParameter>): SelectionState => {
  const selectedIds = new Set<FilterParam>(state.selected[parameter] as FilterParam[]);
  Array.isArray(id) ? id.forEach((filterParam) => selectedIds.add(filterParam)) : selectedIds.add(id);
  return {
    ...state,
    isChanged: true,
    selected: {
      ...state.selected,
      [parameter]: Array.from(selectedIds),
    },
  };
};

const setSelected = (state: SelectionState, {payload: {parameter, id}}: Action<SelectionParameter>): SelectionState => {

  const selected = Array.isArray(id) ? [...id] : [id];
  return {
    ...state,
    isChanged: true,
    selected: {
      ...state.selected,
      [parameter]: selected,
    },
  };
};

const selectSaved = (state: SelectionState, {payload}: Action<SelectionState>): SelectionState => ({
  ...state,
  ...payload,
  isChanged: false,
});

const updatePeriod = (state: SelectionState, {payload}: Action<Period>): SelectionState => (
  {
    ...state,
    isChanged: true,
    selected: {
      ...state.selected,
      period: payload,
    },
  });

const removeSelected = (state: SelectionState, {payload}: Action<SelectionParameter>): SelectionState => {
  const {parameter, id} = payload;
  const selectedIds = state.selected[parameter]! as uuid[];
  return {
    ...state,
    isChanged: true,
    selected: {
      ...state.selected,
      [parameter]: selectedIds.filter((selectedId) => selectedId !== id),
    },
  };
};

type ActionTypes =
  | EmptyAction<string>
  | Action<SelectionParameter>
  | Action<SelectionState>
  | Action<Period>;

export const selection = (state: SelectionState = initialState, action: ActionTypes): SelectionState => {
  switch (action.type) {
    case RESET_SELECTION:
      return {...initialState};
    case ADD_SELECTION:
      return addSelected(state, action as Action<SelectionParameter>);
    case SET_SELECTION:
      return setSelected(state, action as Action<SelectionParameter>);
    case DESELECT_SELECTION:
      return removeSelected(state, action as Action<SelectionParameter>);
    case SELECT_PERIOD:
      return updatePeriod(state, action as Action<Period>);
    case UPDATE_SELECTION:
    case SAVE_SELECTION:
    case SELECT_SAVED_SELECTION:
      return selectSaved(state, action as Action<SelectionState>);
    case CLOSE_SELECTION_PAGE:
      return {
        ...state,
        isChanged: false,
      };
    default:
      return state;
  }
};

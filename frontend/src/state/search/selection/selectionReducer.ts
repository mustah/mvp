import {EmptyAction} from 'react-redux-typescript';
import {Period} from '../../../components/dates/dateModels';
import {EndPoints} from '../../../services/endPoints';
import {Action, uuid} from '../../../types/Types';
import {
  domainModelsDeleteSuccess,
  domainModelsPostSuccess,
  domainModelsPutSuccess,
} from '../../domain-models/domainModelsActions';
import {
  ADD_PARAMETER_TO_SELECTION,
  DESELECT_SELECTION,
  RESET_SELECTION,
  SELECT_PERIOD,
  SELECT_SAVED_SELECTION,
  SET_CURRENT_SELECTION,
  SET_SELECTION,
} from './selectionActions';
import {FilterParam, SelectionParameter, UserSelection} from './selectionModels';

export const initialState: UserSelection = {
  id: -1,
  name: 'all',
  isChanged: false,
  selectionParameters: {
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

const addSelected = (state: UserSelection, {payload: {parameter, id}}: Action<SelectionParameter>): UserSelection => {
  const selectedIds = new Set<FilterParam>(state.selectionParameters[parameter] as FilterParam[]);
  Array.isArray(id) ? id.forEach((filterParam) => selectedIds.add(filterParam)) : selectedIds.add(id);
  return {
    ...state,
    isChanged: true,
    selectionParameters: {
      ...state.selectionParameters,
      [parameter]: Array.from(selectedIds),
    },
  };
};

const setSelected = (state: UserSelection, {payload: {parameter, id}}: Action<SelectionParameter>): UserSelection => {
  const selected = Array.isArray(id) ? [...id] : [id];
  return {
    ...state,
    isChanged: true,
    selectionParameters: {
      ...state.selectionParameters,
      [parameter]: selected,
    },
  };
};

const selectSaved = (state: UserSelection, {payload}: Action<UserSelection>): UserSelection => ({
  ...state,
  ...payload,
  isChanged: false,
});

const updatePeriod = (state: UserSelection, {payload}: Action<Period>): UserSelection => (
  {
    ...state,
    isChanged: true,
    selectionParameters: {
      ...state.selectionParameters,
      period: payload,
    },
  });

const removeSelected = (state: UserSelection, {payload}: Action<SelectionParameter>): UserSelection => {
  const {parameter, id} = payload;
  const selectedIds = state.selectionParameters[parameter]! as uuid[];
  return {
    ...state,
    isChanged: true,
    selectionParameters: {
      ...state.selectionParameters,
      [parameter]: selectedIds.filter((selectedId) => selectedId !== id),
    },
  };
};

type ActionTypes =
  | EmptyAction<string>
  | Action<SelectionParameter>
  | Action<UserSelection>
  | Action<Period>;

export const selection = (state: UserSelection = initialState, action: ActionTypes): UserSelection => {
  switch (action.type) {
    case RESET_SELECTION:
      return {...initialState};
    case ADD_PARAMETER_TO_SELECTION:
      return addSelected(state, action as Action<SelectionParameter>);
    case SET_SELECTION:
      return setSelected(state, action as Action<SelectionParameter>);
    case DESELECT_SELECTION:
      return removeSelected(state, action as Action<SelectionParameter>);
    case SELECT_PERIOD:
      return updatePeriod(state, action as Action<Period>);
    case SET_CURRENT_SELECTION:
    case SELECT_SAVED_SELECTION:
    case domainModelsPostSuccess(EndPoints.userSelections):
    case domainModelsPutSuccess(EndPoints.userSelections):
      return selectSaved(state, action as Action<UserSelection>);
    case domainModelsDeleteSuccess(EndPoints.userSelections):
      const payload: UserSelection = (action as Action<UserSelection>).payload;
      return payload.id === state.id ? initialState : state;
    default:
      return state;
  }
};

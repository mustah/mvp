import {AnyAction} from 'redux';
import {SearchParameter} from './models/searchParameterModels';
import {SELECT_SEARCH_OPTION} from './searchActions';

export interface SearchParametersState {
  area: string[];
  status: string[];
  property: string[];
}

const initialState = {
  area: [],
  status: [],
  property: [],
};

const filterSelectedSearchOption = (state: SearchParametersState, payload: SearchParameter): string[] => {
  const {isChecked, name, value} = payload;
  return isChecked
    ? [...state[name], value]
    : [...state[name]].filter((s: string) => s !== value);
};

export const searchParameters =
  (state: SearchParametersState = initialState, action: AnyAction): SearchParametersState => {
    const {payload} = action;

    switch (action.type) {
      case SELECT_SEARCH_OPTION:
        return {
          ...state,
          [payload.name]: filterSelectedSearchOption(state, payload),
        };
      default:
        return state;
    }
  };

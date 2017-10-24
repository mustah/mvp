import {AnyAction} from 'redux';

interface SearchParameterState {
  selection: any;
}

const initialState = {};

export const searchParameter =
  (state: SearchParameterState = initialState, action: AnyAction): SearchParameterState => {
    switch (action.type) {
      case
      default:
        return state;
    }
  };

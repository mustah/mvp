import {EmptyAction} from 'react-redux-typescript';
import {EndPoints} from '../../services/endPoints';
import {Action, ErrorResponse} from '../../types/Types';
import {LOGOUT_USER} from '../../usecases/auth/authActions';
import {failureAction, requestAction, successAction} from '../common/apiActions';
import {resetReducer} from '../domain-models/domainModelsReducer';
import {NormalizedSelectionTree, SelectionTreeState} from './selectionTreeModels';

export const initialState: SelectionTreeState = {
  isFetching: false,
  isSuccessfullyFetched: false,
  entities: {
    cities: {},
    addresses: {},
    meters: {},
  },
  result: {
    cities: [],
  },
};

type ActionTypes =
  | EmptyAction<string>
  | Action<NormalizedSelectionTree>
  | Action<ErrorResponse>;

export const selectionTree = (
  state: SelectionTreeState = initialState,
  action: ActionTypes,
): SelectionTreeState => {
  switch (action.type) {
    case requestAction(EndPoints.selectionTree):
      return {
        ...state,
        isFetching: true,
      };
    case successAction(EndPoints.selectionTree):
      return {
        ...state,
        isFetching: false,
        isSuccessfullyFetched: true,
        ...(action as Action<NormalizedSelectionTree>).payload,
      };
    case failureAction(EndPoints.selectionTree):
      return {
        ...state,
        isFetching: false,
        isSuccessfullyFetched: false,
        error: (action as Action<ErrorResponse>).payload,
      };
    case LOGOUT_USER:
      return {...initialState};
    default:
      return resetReducer<SelectionTreeState>(state, action, {...initialState});
  }
};

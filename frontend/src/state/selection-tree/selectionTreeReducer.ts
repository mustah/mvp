import {EmptyAction} from 'typesafe-actions/dist/types';
import {EndPoints} from '../../services/endPoints';
import {Action, ErrorResponse} from '../../types/Types';
import {failureAction, requestAction, successAction} from '../api/apiActions';
import {resetReducer} from '../domain-models/domainModelsReducer';
import {NormalizedSelectionTree, SelectionTreeState} from './selectionTreeModels';

const emptyNormalizedState: NormalizedSelectionTree = {
  entities: {
    cities: {},
    addresses: {},
    meters: {},
  },
  result: {
    cities: [],
  },
};

export const initialState: SelectionTreeState = {
  isFetching: false,
  isSuccessfullyFetched: false,
  ...emptyNormalizedState,
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
      const payload = (action as Action<NormalizedSelectionTree>).payload;
      return {
        ...state,
        isFetching: false,
        isSuccessfullyFetched: true,
        ...(payload.result.cities.length ? payload : {...emptyNormalizedState}),
      };
    case failureAction(EndPoints.selectionTree):
      return {
        ...state,
        isFetching: false,
        isSuccessfullyFetched: false,
        error: (action as Action<ErrorResponse>).payload,
      };
    default:
      return resetReducer<SelectionTreeState>(state, action, initialState);
  }
};

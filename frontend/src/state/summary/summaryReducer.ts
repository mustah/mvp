import {Location} from 'history';
import {LOCATION_CHANGE} from 'react-router-redux';
import {EmptyAction} from 'typesafe-actions/dist/types';
import {routes} from '../../app/routes';
import {EndPoints} from '../../services/endPoints';
import {Action, ErrorResponse} from '../../types/Types';
import {SEARCH} from '../../usecases/search/searchActions';
import {failureAction, requestAction, successAction} from '../api/apiActions';
import {
  domainModelsPaginatedDeleteFailure,
  domainModelsPaginatedDeleteRequest,
  domainModelsPaginatedDeleteSuccess
} from '../domain-models-paginated/paginatedDomainModelsEntityActions';
import {resetReducer} from '../domain-models/domainModelsReducer';
import {SelectionSummary, SummaryState} from './summaryModels';

export const initialState: SummaryState = {
  isFetching: false,
  isSuccessfullyFetched: false,
  payload: {numMeters: 0, numCities: 0, numAddresses: 0},
};

type ActionTypes =
  | EmptyAction<string>
  | Action<SelectionSummary | ErrorResponse | Location>;

const getPathname = (action: ActionTypes): string => (action as Action<Location>).payload.pathname;

export const summary = (state: SummaryState = initialState, action: ActionTypes): SummaryState => {
  switch (action.type) {
    case requestAction(EndPoints.summaryMeters):
      return {
        ...state,
        isFetching: true,
      };
    case successAction(EndPoints.summaryMeters):
      return {
        ...state,
        isFetching: false,
        isSuccessfullyFetched: true,
        payload: (action as Action<SelectionSummary>).payload,
      };
    case failureAction(EndPoints.summaryMeters):
      return {
        ...state,
        isFetching: false,
        isSuccessfullyFetched: false,
        error: (action as Action<ErrorResponse>).payload,
      };
    case domainModelsPaginatedDeleteRequest(EndPoints.meters):
      return {
        ...state,
        isFetching: true,
      };
    case domainModelsPaginatedDeleteSuccess(EndPoints.meters):
      return {
        ...state,
        isFetching: false,
        isSuccessfullyFetched: true,
        payload: {...state.payload, numMeters: state.payload.numMeters - 1}
      };
    case domainModelsPaginatedDeleteFailure(EndPoints.meters):
      return {
        ...state,
        isFetching: false,
        isSuccessfullyFetched: false,
      };
    case LOCATION_CHANGE:
      return getPathname(action) !== routes.searchResult ? initialState : state;
    case SEARCH:
      return initialState;
    default:
      return resetReducer(state, action, initialState);
  }
};

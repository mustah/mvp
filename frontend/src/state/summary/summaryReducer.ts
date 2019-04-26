import {Location} from 'history';
import {ActionType, getType} from 'typesafe-actions';
import {EmptyAction} from 'typesafe-actions/dist/type-helpers';
import {isOnSearchPage} from '../../app/routes';
import {resetReducer} from '../../reducers/resetReducer';
import {EndPoints} from '../../services/endPoints';
import {Action, ErrorResponse, Sectors} from '../../types/Types';
import {clearAction, failureAction, requestAction, successAction} from '../api/apiActions';
import {
  domainModelsPaginatedDeleteFailure,
  domainModelsPaginatedDeleteRequest,
  domainModelsPaginatedDeleteSuccess
} from '../domain-models-paginated/paginatedDomainModelsEntityActions';
import {locationChange} from '../location/locationActions';
import {search} from '../search/searchActions';
import {SelectionSummary, SummaryState} from './summaryModels';

export const initialState: SummaryState = {
  isFetching: false,
  isSuccessfullyFetched: false,
  payload: {numMeters: 0, numCities: 0, numAddresses: 0},
};

type ActionTypes =
  | EmptyAction<string>
  | Action<SelectionSummary | ErrorResponse>
  | ActionType<typeof locationChange | typeof search>;

export const summaryFor =
  (sector: Sectors) =>
  (state: SummaryState = initialState, action: ActionTypes): SummaryState => {
  switch (action.type) {
    case requestAction(sector):
      return {
        ...state,
        isFetching: true,
      };
    case successAction(sector):
      return {
        ...state,
        isFetching: false,
        isSuccessfullyFetched: true,
        payload: (action as Action<SelectionSummary>).payload,
      };
    case failureAction(sector):
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
    case getType(locationChange):
      return isOnSearchPage((action as Action<Location>).payload) ? state : initialState;
    case getType(search):
    case clearAction(sector):
      return initialState;
    default:
      return resetReducer(state, action, initialState);
  }
};

export const summary = summaryFor(Sectors.summary);

export const organisationSummary = summaryFor(Sectors.organisationSummary);

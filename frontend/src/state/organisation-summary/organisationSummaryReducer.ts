import {ActionType} from 'typesafe-actions';
import {EmptyAction} from 'typesafe-actions/dist/type-helpers';
import {resetReducer} from '../../reducers/resetReducer';
import {EndPoints} from '../../services/endPoints';
import {Action, ErrorResponse, Sectors} from '../../types/Types';
import {clearAction, failureAction, requestAction, successAction} from '../api/apiActions';
import {
  domainModelsPaginatedDeleteFailure,
  domainModelsPaginatedDeleteRequest,
  domainModelsPaginatedDeleteSuccess
} from '../domain-models-paginated/paginatedDomainModelsEntityActions';
import {search} from '../search/searchActions';
import {SummaryState} from './organisationSummaryModels';

export const initialState: SummaryState = {
  isFetching: false,
  isSuccessfullyFetched: false,
  numMeters: 0,
};

type ActionTypes = | EmptyAction<string> | Action<number> | ActionType<typeof search>;

export const organisationSummary = (state: SummaryState = initialState, action: ActionTypes): SummaryState => {
  switch (action.type) {
    case requestAction(Sectors.organisationSummary):
      return {
        ...state,
        isFetching: true,
      };
    case successAction(Sectors.organisationSummary):
      return {
        ...state,
        isFetching: false,
        isSuccessfullyFetched: true,
        numMeters: (action as Action<number>).payload,
      };
    case failureAction(Sectors.organisationSummary):
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
        numMeters: state.numMeters - 1,
      };
    case domainModelsPaginatedDeleteFailure(EndPoints.meters):
      return {
        ...state,
        isFetching: false,
        isSuccessfullyFetched: false,
      };
    case clearAction(Sectors.organisationSummary):
      return initialState;
    default:
      return resetReducer(state, action, initialState);
  }
};

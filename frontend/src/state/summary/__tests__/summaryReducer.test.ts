import {getType} from 'typesafe-actions';
import {toLocation} from '../../../__tests__/testDataFactory';
import {routes} from '../../../app/routes';
import {EndPoints} from '../../../services/endPoints';
import {Sectors} from '../../../types/Types';
import {logoutUser} from '../../../usecases/auth/authActions';
import {makeActionsOf, RequestHandler} from '../../api/apiActions';
import {Meter} from '../../domain-models-paginated/meter/meterModels';
import {makePaginatedDeleteRequestActions} from '../../domain-models-paginated/paginatedDomainModelsEntityActions';
import {locationChange} from '../../location/locationActions';
import {search} from '../../search/searchActions';
import {makeMeterQuery} from '../../search/searchModels';
import {
  addParameterToSelection,
  deselectSelection,
  resetSelection,
  selectSavedSelectionAction,
} from '../../user-selection/userSelectionActions';
import {SelectionSummary, SummaryState} from '../summaryModels';
import {initialState, summary} from '../summaryReducer';

describe('summaryReducer', () => {

  const actions: RequestHandler<SelectionSummary> =
    makeActionsOf<SelectionSummary>(Sectors.summary);

  describe('unknown action type', () => {

    it('has initial state for unknown action', () => {
      expect(summary(undefined, {type: 'unknown', payload: 'nothing'})).toEqual(initialState);
    });

    it('returns the previous state', () => {
      const prevState: SummaryState = {...initialState, isFetching: true};
      expect(summary(prevState, {type: 'unknown', payload: 'nothing'})).toEqual(
        {
          ...initialState,
          isFetching: true,
        });
    });
  });

  describe('request action type', () => {

    it('returns summary result for request action ', () => {
      expect(summary(initialState, actions.request())).toEqual(
        {
          isFetching: true,
          isSuccessfullyFetched: false,
          payload: initialState.payload,
        });
    });

    it('return summary result with empty request parameters', () => {
      expect(summary(initialState, actions.request())).toEqual({
        isFetching: true,
        isSuccessfullyFetched: false,
        payload: initialState.payload,
      });
    });
  });

  describe('success action type', () => {

    it('has payload', () => {
      const payload: SelectionSummary = {numCities: 1, numAddresses: 2, numMeters: 2};

      expect(summary(initialState, actions.success(payload))).toEqual(
        {
          isFetching: false,
          isSuccessfullyFetched: true,
          payload,
        });
    });
  });

  describe('failure action type', () => {

    it('is not successfully fetched on failure', () => {
      const payload: SelectionSummary = {numCities: 1, numAddresses: 2, numMeters: 2};
      const state = {
        isFetching: false,
        isSuccessfullyFetched: false,
        payload,
      };

      expect(summary(state, actions.failure({message: 'failed'}))).toEqual(
        {
          isFetching: false,
          isSuccessfullyFetched: false,
          payload,
          error: {message: 'failed'},
        },
      );
    });
  });

  describe('selection changed', () => {

    it('resets to initial state', () => {
      const payload: SelectionSummary = {numCities: 1, numAddresses: 2, numMeters: 2};
      const state = {
        isFetching: false,
        isSuccessfullyFetched: true,
        payload,
      };

      [
        getType(selectSavedSelectionAction),
        getType(addParameterToSelection),
        getType(deselectSelection),
        getType(resetSelection),
      ].forEach((actionThatResets: string) => {
        expect(summary(state, {type: actionThatResets})).toEqual(initialState);
      });
    });
  });

  describe('integration', () => {

    it('reduces normal fetch successfully action', () => {
      const payload: SelectionSummary = {numCities: 1, numAddresses: 2, numMeters: 2};

      let state: SummaryState = summary(initialState, actions.request());
      state = summary(state, actions.success(payload));

      expect(state).toEqual(
        {
          isFetching: false,
          isSuccessfullyFetched: true,
          payload,
        });
    });

    it('reduces from success to failure', () => {
      const payload: SelectionSummary = {numCities: 1, numAddresses: 2, numMeters: 2};
      const error = {message: 'failed for some reason'};

      let state: SummaryState = summary(initialState, actions.request());
      state = summary(state, actions.success(payload));
      state = summary(state, actions.failure(error));

      expect(state).toEqual(
        {
          isFetching: false,
          isSuccessfullyFetched: false,
          payload,
          error,
        });
    });
  });

  describe('logout user', () => {

    it('resets state to initial state', () => {
      let state: SummaryState = summary(initialState, actions.request());
      state = summary(state, actions.success({numCities: 1, numAddresses: 2, numMeters: 2}));
      state = summary(state, actions.failure({message: 'failed for some reason'}));

      state = summary(state, logoutUser(undefined));

      expect(state).toEqual({...initialState});
    });
  });

  describe('delete meter', () => {
    const deleteRequestActions = makePaginatedDeleteRequestActions(EndPoints.meters);

    it('should show loading animation when delete meter request is dispatched', () => {
      const someState: SummaryState = {...initialState};

      const state: SummaryState = summary(someState, deleteRequestActions.request());

      const expected: SummaryState = {...initialState, isFetching: true};
      expect(state).toEqual(expected);
    });

    it('should decrease meter count upon success', () => {
      const someState: SummaryState = {
        ...initialState,
        isFetching: true,
        payload: {...initialState.payload, numMeters: 2}
      };

      const meter = {id: 1};

      const state: SummaryState = summary(someState, deleteRequestActions.success(meter as Meter));

      const expected: SummaryState = {
        ...initialState,
        isFetching: false,
        isSuccessfullyFetched: true,
        payload: {...initialState.payload, numMeters: 1},
      };
      expect(state).toEqual(expected);
    });

    it('should reset state when delete meter fails', () => {
      const someState: SummaryState = {
        ...initialState,
        isFetching: true,
        payload: {...initialState.payload, numMeters: 2}
      };

      const state: SummaryState = summary(someState, deleteRequestActions.failure({id: 1, message: 'not ok'}));

      const expected: SummaryState = {
        ...initialState,
        payload: {...initialState.payload, numMeters: 2},
      };
      expect(state).toEqual(expected);
    });
  });

  describe('search query', () => {

    it('should reset summary to initial state when global search is performed', () => {
      const someState: SummaryState = {
        ...initialState,
        payload: {...initialState.payload, numMeters: 2}
      };

      const state: SummaryState = summary(someState, search(makeMeterQuery('123')));

      expect(state).toEqual(initialState);
    });

    it('reset search query when location changes to dashboard page', () => {
      let state: SummaryState = {
        ...initialState,
        payload: {...initialState.payload, numMeters: 2}
      };

      state = summary(state, locationChange(toLocation(routes.dashboard)));

      expect(state).toEqual(initialState);
    });

    it('keeps the state when location changes to search result page', () => {
      const state: SummaryState = {
        ...initialState,
        payload: {...initialState.payload, numMeters: 2}
      };

      const nextState = summary(state, locationChange(toLocation(routes.searchResult)));

      expect(nextState).toBe(state);
    });
  });

});

import {getType} from 'typesafe-actions';
import {EndPoints} from '../../../services/endPoints';
import {Sectors} from '../../../types/Types';
import {logoutUser} from '../../../usecases/auth/authActions';
import {makeActionsOf, RequestHandler} from '../../api/apiActions';
import {Meter} from '../../domain-models-paginated/meter/meterModels';
import {makePaginatedDeleteRequestActions} from '../../domain-models-paginated/paginatedDomainModelsEntityActions';
import {search} from '../../search/searchActions';
import {makeMeterQuery} from '../../search/searchModels';
import {
  addParameterToSelection,
  deselectSelection,
  resetSelection,
  selectSavedSelectionAction,
} from '../../user-selection/userSelectionActions';
import {SummaryState} from '../organisationSummaryModels';
import {initialState, organisationSummary} from '../organisationSummaryReducer';

describe('organisation summary reducer', () => {

  const actions: RequestHandler<number> = makeActionsOf<number>(Sectors.organisationSummary);

  describe('unknown action type', () => {

    it('has initial state for unknown action', () => {
      expect(organisationSummary(undefined, {type: 'unknown', payload: 'nothing'})).toEqual(initialState);
    });

    it('returns the previous state', () => {
      const prevState: SummaryState = {...initialState, isFetching: true};
      expect(organisationSummary(prevState, {type: 'unknown', payload: 'nothing'})).toEqual(
        {
          ...initialState,
          isFetching: true,
        });
    });
  });

  describe('request action type', () => {

    it('returns summary result for request action ', () => {
      expect(organisationSummary(initialState, actions.request())).toEqual(
        {
          isFetching: true,
          isSuccessfullyFetched: false,
          numMeters: 0,
        });
    });

    it('return summary result with empty request parameters', () => {
      expect(organisationSummary(initialState, actions.request())).toEqual({
        isFetching: true,
        isSuccessfullyFetched: false,
        numMeters: 0,
      });
    });
  });

  describe('success action type', () => {

    it('has payload', () => {
      expect(organisationSummary(initialState, actions.success(2))).toEqual(
        {
          isFetching: false,
          isSuccessfullyFetched: true,
          numMeters: 2,
        });
    });
  });

  describe('failure action type', () => {

    it('is not successfully fetched on failure', () => {
      const state = {
        isFetching: false,
        isSuccessfullyFetched: false,
        numMeters: 2,
      };

      expect(organisationSummary(state, actions.failure({message: 'failed'}))).toEqual(
        {
          isFetching: false,
          isSuccessfullyFetched: false,
          numMeters: 2,
          error: {message: 'failed'},
        },
      );
    });
  });

  describe('selection changed', () => {

    it('resets to initial state', () => {
      const state: SummaryState = {
        isFetching: false,
        isSuccessfullyFetched: true,
        numMeters: 2
      };

      [
        getType(selectSavedSelectionAction),
        getType(addParameterToSelection),
        getType(deselectSelection),
        getType(resetSelection),
      ].forEach((actionThatResets: string) => {
        expect(organisationSummary(state, {type: actionThatResets})).toEqual(initialState);
      });
    });
  });

  describe('integration', () => {

    it('reduces normal fetch successfully action', () => {
      const prevState: SummaryState = organisationSummary(initialState, actions.request());

      const state = organisationSummary(prevState, actions.success(2));

      expect(state).toEqual({
        isFetching: false,
        isSuccessfullyFetched: true,
        numMeters: 2,
      });
    });

    it('reduces from success to failure', () => {
      const error = {message: 'failed for some reason'};

      let state: SummaryState = organisationSummary(initialState, actions.request());
      state = organisationSummary(state, actions.success(2));
      state = organisationSummary(state, actions.failure(error));

      expect(state).toEqual({
        isFetching: false,
        isSuccessfullyFetched: false,
        numMeters: 2,
        error,
      });
    });
  });

  describe('logout user', () => {

    it('resets state to initial state', () => {
      let state: SummaryState = organisationSummary(initialState, actions.request());
      state = organisationSummary(state, actions.success(23));
      state = organisationSummary(state, actions.failure({message: 'failed for some reason'}));

      state = organisationSummary(state, logoutUser(undefined));

      expect(state).toEqual({...initialState});
    });
  });

  describe('delete meter', () => {
    const deleteRequestActions = makePaginatedDeleteRequestActions(EndPoints.meters);

    it('should show loading animation when delete meter request is dispatched', () => {
      const someState: SummaryState = {...initialState};

      const state: SummaryState = organisationSummary(someState, deleteRequestActions.request());

      const expected: SummaryState = {...initialState, isFetching: true};
      expect(state).toEqual(expected);
    });

    it('should decrease meter count upon success', () => {
      const someState: SummaryState = {
        ...initialState,
        isFetching: true,
        numMeters: 2,
      };

      const meter = {id: 1};

      const state: SummaryState = organisationSummary(someState, deleteRequestActions.success(meter as Meter));

      const expected: SummaryState = {
        ...initialState,
        isFetching: false,
        isSuccessfullyFetched: true,
        numMeters: 1,
      };
      expect(state).toEqual(expected);
    });

    it('should reset state when delete meter fails', () => {
      const someState: SummaryState = {
        ...initialState,
        isFetching: true,
        numMeters: 2,
      };

      const state: SummaryState = organisationSummary(
        someState,
        deleteRequestActions.failure({id: 1, message: 'not ok'})
      );

      const expected: SummaryState = {...initialState, numMeters: 2};
      expect(state).toEqual(expected);
    });
  });

  describe('search query', () => {

    it('should reset summary to initial state when global search is performed', () => {
      const someState: SummaryState = {...initialState, numMeters: 2};

      const state: SummaryState = organisationSummary(someState, search(makeMeterQuery('123')));

      expect(state).toEqual(initialState);
    });

  });

});

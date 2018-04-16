import {EndPoints} from '../../../services/endPoints';
import {
  ADD_PARAMETER_TO_SELECTION,
  DESELECT_SELECTION,
  RESET_SELECTION,
  SELECT_PERIOD,
  SELECT_SAVED_SELECTION,
  SET_SELECTION,
  SET_CURRENT_SELECTION,
} from '../../search/selection/selectionActions';
import {makeActionsOf, RequestHandler} from '../summaryApiActions';
import {SelectionSummary, SummaryState} from '../summaryModels';
import {initialState, summary} from '../summaryReducer';

describe('summaryReducer', () => {

  const actions: RequestHandler<SelectionSummary> =
    makeActionsOf<SelectionSummary>(EndPoints.summaryMeters);

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

      expect(summary(state, {type: SELECT_SAVED_SELECTION})).toEqual(initialState);
      expect(summary(state, {type: ADD_PARAMETER_TO_SELECTION})).toEqual(initialState);
      expect(summary(state, {type: DESELECT_SELECTION})).toEqual(initialState);
      expect(summary(state, {type: SET_CURRENT_SELECTION})).toEqual(initialState);
      expect(summary(state, {type: RESET_SELECTION})).toEqual(initialState);
      expect(summary(state, {type: SET_SELECTION})).toEqual(initialState);
      expect(summary(state, {type: SELECT_PERIOD})).toEqual(initialState);
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

});

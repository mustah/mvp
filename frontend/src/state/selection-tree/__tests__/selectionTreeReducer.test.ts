import {normalize} from 'normalizr';
import {EndPoints} from '../../../services/endPoints';
import {LOGOUT_USER} from '../../../usecases/auth/authActions';
import {makeActionsOf, RequestHandler} from '../../common/apiActions';
import {
  ADD_PARAMETER_TO_SELECTION,
  DESELECT_SELECTION,
  RESET_SELECTION,
  SELECT_PERIOD,
  SELECT_SAVED_SELECTION,
} from '../../user-selection/userSelectionActions';
import {NormalizedSelectionTree, SelectionTreeState} from '../selectionTreeModels';
import {initialState, selectionTree} from '../selectionTreeReducer';
import {selectionTreeSchema} from '../selectionTreeSchemas';

describe('selectionTreeReducer', () => {

  const responseFromApi = {
    cities: [
      {
        id: 'sweden,kungsbacka', name: 'kungsbacka', addresses: [
          {
            name: 'kabelgatan 1', meters: [
              {id: 1, name: 'extId1'},
              {id: 2, name: 'extId2'},
            ],
          },
          {
            name: 'kungsgatan 42', meters: [
              {id: 5, name: 'extId5'},
              {id: 6, name: 'extId6'},
            ],
          },
        ],
      },
      {
        id: 'sweden,gothenburg', name: 'gothenburg', addresses: [
          {
            name: 'kungsgatan 42', meters: [
              {id: 3, name: 'extId3'},
              {id: 4, name: 'extId4'},
            ],
          },
        ],
      },
    ],
  };
  const normalizedResponse = normalize(responseFromApi, selectionTreeSchema);

  const actions: RequestHandler<NormalizedSelectionTree> =
    makeActionsOf<NormalizedSelectionTree>(EndPoints.selectionTree);

  describe('unknown action type', () => {

    it('has initial state for unknown action', () => {
      expect(selectionTree(undefined, {type: 'unknown', payload: 'nothing'})).toEqual(initialState);
    });

    it('returns the previous state', () => {
      const prevState: SelectionTreeState = {...initialState, isFetching: true};
      expect(selectionTree(prevState, {type: 'unknown', payload: 'nothing'})).toEqual(
        {
          ...initialState,
          isFetching: true,
        });
    });
  });

  describe('request action type', () => {

    it('returns selectionTree for request action ', () => {
      expect(selectionTree(initialState, actions.request())).toEqual(
        {
          isFetching: true,
          isSuccessfullyFetched: false,
          entities: {...initialState.entities},
          result: {...initialState.result},
        });
    });

  });

  describe('success action type', () => {

    it('has payload', () => {
      const payload: NormalizedSelectionTree = normalizedResponse;

      expect(selectionTree(initialState, actions.success(payload))).toEqual(
        {
          isFetching: false,
          isSuccessfullyFetched: true,
          ...payload,
        });
    });
  });

  describe('failure action type', () => {

    it('is not successfully fetched on failure', () => {
      const payload: NormalizedSelectionTree = normalizedResponse;
      const state = {
        isFetching: false,
        isSuccessfullyFetched: false,
        ...payload,
      };

      expect(selectionTree(state, actions.failure({message: 'failed'}))).toEqual(
        {
          isFetching: false,
          isSuccessfullyFetched: false,
          ...payload,
          error: {message: 'failed'},
        },
      );
    });
  });

  describe('selection changed', () => {

    it('resets to initial state', () => {
      const state = {
        isFetching: false,
        isSuccessfullyFetched: true,
        ...normalizedResponse,
      };

      expect(selectionTree(state, {type: SELECT_SAVED_SELECTION})).toEqual(initialState);
      expect(selectionTree(state, {type: ADD_PARAMETER_TO_SELECTION})).toEqual(initialState);
      expect(selectionTree(state, {type: DESELECT_SELECTION})).toEqual(initialState);
      expect(selectionTree(state, {type: RESET_SELECTION})).toEqual(initialState);
      expect(selectionTree(state, {type: SELECT_PERIOD})).toEqual(initialState);
    });
  });

  describe('integration', () => {

    it('reduces normal fetch successfully action', () => {
      const payload: NormalizedSelectionTree = normalizedResponse;

      let state: SelectionTreeState = selectionTree(initialState, actions.request());
      state = selectionTree(state, actions.success(payload));

      expect(state).toEqual(
        {
          isFetching: false,
          isSuccessfullyFetched: true,
          ...payload,
        });
    });

    it('reduces from success to failure', () => {
      const payload: NormalizedSelectionTree = normalizedResponse;
      const error = {message: 'failed for some reason'};

      let state: SelectionTreeState = selectionTree(initialState, actions.request());
      state = selectionTree(state, actions.success(payload));
      state = selectionTree(state, actions.failure(error));

      expect(state).toEqual(
        {
          isFetching: false,
          isSuccessfullyFetched: false,
          ...payload,
          error,
        });
    });
  });

  describe('logout user', () => {

    it('resets state to initial state', () => {
      let state: SelectionTreeState = selectionTree(initialState, actions.request());

      state = selectionTree(state, {type: LOGOUT_USER});

      expect(state).toEqual(initialState);
    });
  });

});

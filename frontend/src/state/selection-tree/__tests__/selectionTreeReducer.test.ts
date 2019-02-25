import {EndPoints} from '../../../services/endPoints';
import {uuid} from '../../../types/Types';
import {LOGOUT_USER} from '../../../usecases/auth/authActions';
import {makeActionsOf, RequestHandler} from '../../api/apiActions';
import {Medium} from '../../ui/graph/measurement/measurementModels';
import {
  ADD_PARAMETER_TO_SELECTION,
  DESELECT_SELECTION,
  RESET_SELECTION,
  SELECT_SAVED_SELECTION,
} from '../../user-selection/userSelectionActions';
import {NormalizedSelectionTree, SelectionTreeState} from '../selectionTreeModels';
import {initialState, selectionTree} from '../selectionTreeReducer';
import {selectionTreeDataFormatter} from '../selectionTreeSchemas';

describe('selectionTreeReducer', () => {

  const responseFromApi = {
    cities: [
      {
        id: 'sweden,kungsbacka',
        name: 'kungsbacka',
        medium: ['District heating'],
        addresses: [
          {
            name: 'kabelgatan 1',
            meters: [
              {
                id: 1,
                medium: 'District heating',
                name: 'extId1',
              },
              {
                id: 2,
                medium: 'District heating',
                name: 'extId2',
              },
            ],
          },
          {
            name: 'kungsgatan 42',
            meters: [
              {
                id: 5,
                medium: 'District heating',
                name: 'extId5',
              },
              {
                id: 6,
                medium: 'District heating',
                name: 'extId6',
              },
            ],
          },
        ],
      },
      {
        id: 'sweden,gothenburg',
        name: 'gothenburg',
        medium: ['District heating'],
        addresses: [
          {
            name: 'kungsgatan 42',
            meters: [
              {
                id: 3,
                medium: 'District heating',
                name: 'extId3',
              },
              {
                id: 4,
                medium: 'District heating',
                name: 'extId4',
              },
            ],
          },
        ],
      },
    ],
  };
  const normalizedResponse: NormalizedSelectionTree = selectionTreeDataFormatter(responseFromApi);

  const actions: RequestHandler<NormalizedSelectionTree> =
    makeActionsOf<NormalizedSelectionTree>(EndPoints.selectionTree);

  describe('unknown action type', () => {

    it('has initial state for unknown action', () => {
      expect(selectionTree(undefined, {type: 'unknown', payload: 'nothing'})).toEqual(initialState);
    });

    it('returns the previous state', () => {
      const prevState: SelectionTreeState = {...initialState, isFetching: true};
      expect(selectionTree(prevState, {type: 'unknown', payload: 'nothing'})).toEqual(prevState);
    });
  });

  describe('request action type', () => {

    it('returns selectionTree for request action ', () => {
      const expected: SelectionTreeState = {
        isFetching: true,
        isSuccessfullyFetched: false,
        entities: {...initialState.entities},
        result: {...initialState.result},
      };
      expect(selectionTree(initialState, actions.request())).toEqual(expected);
    });

  });

  describe('success action type', () => {

    it('has payload', () => {
      const payload: NormalizedSelectionTree = normalizedResponse;

      const expected: SelectionTreeState = {
        isFetching: false,
        isSuccessfullyFetched: true,
        ...payload,
      };
      expect(selectionTree(initialState, actions.success(payload))).toEqual(expected);
    });

    describe('missing or empty medium', () => {

      it('handles missing medium for meters', () => {
        const missingMeterMedium = {...responseFromApi};
        delete missingMeterMedium.cities[0].addresses[0].meters[0].medium;
        const firstMetersId: uuid = missingMeterMedium.cities[0].addresses[0].meters[0].id;

        const payload: NormalizedSelectionTree = selectionTreeDataFormatter(responseFromApi);

        const stateAfterSuccess: SelectionTreeState = selectionTree(initialState, actions.success(payload));
        const {id, medium} = stateAfterSuccess.entities.meters[firstMetersId];
        expect(id).toEqual(firstMetersId);
        expect(medium).toEqual(Medium.unknown);
      });

      it('handles empty medium for meters', () => {
        const missingMeterMedium = {...responseFromApi};
        missingMeterMedium.cities[0].addresses[0].meters[0].medium = '';
        const firstMetersId: uuid = missingMeterMedium.cities[0].addresses[0].meters[0].id;

        const payload: NormalizedSelectionTree = selectionTreeDataFormatter(responseFromApi);

        const stateAfterSuccess: SelectionTreeState = selectionTree(initialState, actions.success(payload));
        const {id, medium} = stateAfterSuccess.entities.meters[firstMetersId];
        expect(id).toEqual(firstMetersId);
        expect(medium).toEqual(Medium.unknown);
      });

      it('handles missing medium for cities', () => {
        const missingMeterMedium = {...responseFromApi};
        delete missingMeterMedium.cities[0].medium;
        const firstCitysId: uuid = missingMeterMedium.cities[0].id;

        const payload: NormalizedSelectionTree = selectionTreeDataFormatter(responseFromApi);

        const stateAfterSuccess: SelectionTreeState = selectionTree(initialState, actions.success(payload));
        const {id, medium} = stateAfterSuccess.entities.cities[firstCitysId];
        expect(id).toEqual(firstCitysId);
        expect(medium).toEqual([]);
      });

      it('handles empty medium for cities', () => {
        const missingMeterMedium = {...responseFromApi};
        missingMeterMedium.cities[0].medium = [];
        const firstCitysId: uuid = missingMeterMedium.cities[0].id;

        const payload: NormalizedSelectionTree = selectionTreeDataFormatter(responseFromApi);

        const stateAfterSuccess: SelectionTreeState = selectionTree(initialState, actions.success(payload));
        const {id, medium} = stateAfterSuccess.entities.cities[firstCitysId];
        expect(id).toEqual(firstCitysId);
        expect(medium).toEqual([]);
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

      const expected: SelectionTreeState = {
        isFetching: false,
        isSuccessfullyFetched: false,
        ...payload,
        error: {message: 'failed'},
      };
      expect(selectionTree(state, actions.failure({message: 'failed'}))).toEqual(expected);
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
    });
  });

  describe('integration', () => {

    it('reduces normal fetch successfully action', () => {
      const payload: NormalizedSelectionTree = normalizedResponse;

      const state: SelectionTreeState = selectionTree(initialState, actions.request());
      const afterSuccess: SelectionTreeState = selectionTree(state, actions.success(payload));

      const expected: SelectionTreeState = {
        isFetching: false,
        isSuccessfullyFetched: true,
        ...payload,
      };
      expect(afterSuccess).toEqual(expected);
    });

    it('reduces from success to failure', () => {
      const payload: NormalizedSelectionTree = normalizedResponse;
      const error = {message: 'failed for some reason'};

      const state: SelectionTreeState = selectionTree(initialState, actions.request());
      const success: SelectionTreeState = selectionTree(state, actions.success(payload));
      const failure: SelectionTreeState = selectionTree(success, actions.failure(error));

      const expected: SelectionTreeState = {
        isFetching: false,
        isSuccessfullyFetched: false,
        ...payload,
        error,
      };
      expect(failure).toEqual(expected);
    });
  });

  describe('logout user', () => {

    it('resets state to initial state', () => {
      const state: SelectionTreeState = selectionTree(initialState, actions.request());

      const loggedOut = selectionTree(state, {type: LOGOUT_USER});

      expect(loggedOut).toEqual(initialState);
    });
  });

});

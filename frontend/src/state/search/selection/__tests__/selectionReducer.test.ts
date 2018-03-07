import {Period} from '../../../../components/dates/dateModels';
import {IdNamed} from '../../../../types/Types';
import {
  ADD_SELECTION,
  DESELECT_SELECTION,
  RESET_SELECTION,
  SELECT_SAVED_SELECTION,
  SET_SELECTION,
} from '../selectionActions';
import {ParameterName, SelectionParameter, SelectionState} from '../selectionModels';
import {initialState, selection} from '../selectionReducer';

describe('selectionReducer', () => {

  const mockPayload: SelectionState = {
    id: 5,
    name: 'something else',
    isChanged: false,
    selected: {
      cities: ['sweden,gothenburg', 'sweden,stockholm'],
      addresses: [1, 2, 3],
      period: Period.latest,
    },
  };

  const gothenburg: IdNamed = {id: 'sweden,gothenburg', name: 'gothenburg'};
  const stockholm: IdNamed = {id: 'sweden,stockholm', name: 'stockholm'};
  const malmo: IdNamed = {id: 'sweden,malmo', name: 'malmo'};

  describe('select saved selections', () => {

    it('replaces current selection', () => {
      const state = selection(initialState, {type: SELECT_SAVED_SELECTION, payload: mockPayload});

      expect(state).toEqual({...mockPayload, isChanged: false});
    });
  });

  describe('update current selection', () => {

    it('adds to selected list', () => {
      const state = {...initialState};
      const stockholm: IdNamed = {id: 'sweden,stockholm', name: 'stockholm'};
      const selectionParameters: SelectionParameter = {
        ...stockholm,
        parameter: ParameterName.cities,
      };

      expect(selection(state, {type: ADD_SELECTION, payload: selectionParameters})).toEqual({
        ...initialState,
        isChanged: true,
        selected: {
          ...state.selected,
          cities: [stockholm.id],
        },
      });
    });

    it('adds array of filterParams to selected list', () => {

      const selectionParameterItem: SelectionParameter = {parameter: ParameterName.cities, ...gothenburg};
      const selectionParametersArray: SelectionParameter = {
        parameter: ParameterName.cities,
        id: [
          stockholm.id,
          malmo.id,
        ],
      };

      const intermediateState: SelectionState = selection(initialState, {
        type: ADD_SELECTION,
        payload: selectionParameterItem,
      });
      const finalState: SelectionState = selection(intermediateState, {
        type: ADD_SELECTION,
        payload: selectionParametersArray,
      });

      expect(finalState).toEqual({
        ...intermediateState,
        isChanged: true,
        selected: {
          ...intermediateState.selected,
          cities: [gothenburg.id, stockholm.id, malmo.id],
        },
      });
    });

    it('set filterParam as selected list', () => {

      const selectionParameterInitial: SelectionParameter = {parameter: ParameterName.cities, ...gothenburg};
      const selectionParametersFinal: SelectionParameter = {
        parameter: ParameterName.cities,
        ...stockholm,
      };

      const intermediateState: SelectionState = selection(initialState, {
          type: SET_SELECTION,
          payload: selectionParameterInitial,
        })
      ;
      const finalState: SelectionState = selection(intermediateState, {
          type: SET_SELECTION,
          payload: selectionParametersFinal,
        })
      ;

      expect(finalState).toEqual({
        ...intermediateState,
        isChanged: true,
        selected: {
          ...intermediateState.selected,
          cities: [stockholm.id],
        },
      });
    });

    it('set array of filterParams as selected list', () => {

      const selectionParameterInitial: SelectionParameter = {parameter: ParameterName.cities, ...gothenburg};
      const selectionParametersFinal: SelectionParameter = {
        parameter: ParameterName.cities,
        id: [
          stockholm.id,
          malmo.id,
        ],
      };

      const intermediateState: SelectionState = selection(initialState, {
        type: SET_SELECTION,
        payload: selectionParameterInitial,
      });
      const finalState: SelectionState = selection(intermediateState, {
        type: SET_SELECTION,
        payload: selectionParametersFinal,
      });

      expect(finalState).toEqual({
        ...intermediateState,
        isChanged: true,
        selected: {
          ...intermediateState.selected,
          cities: [stockholm.id, malmo.id],
        },
      });
    });

  });

  describe('reset selection', () => {

    it('resets current selection', () => {
      let state = selection(initialState, {type: SELECT_SAVED_SELECTION, payload: mockPayload});

      expect(state).not.toEqual(initialState);

      state = selection(state, {type: RESET_SELECTION});

      expect(state).toEqual(initialState);
    });
  });

  describe('deselect', () => {

    it('will deselect selected city', () => {
      const parameter: SelectionParameter = {
        parameter: ParameterName.cities,
        ...gothenburg,
      };

      const state = selection(mockPayload, {type: DESELECT_SELECTION, payload: parameter});

      expect(state).toEqual({
        id: 5,
        name: 'something else',
        isChanged: true,
        selected: {cities: ['sweden,stockholm'], addresses: [1, 2, 3], period: Period.latest},
      });
    });
  });
});

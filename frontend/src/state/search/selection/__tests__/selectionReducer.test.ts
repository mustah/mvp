import {Period} from '../../../../components/dates/dateModels';
import {EndPoints} from '../../../../services/endPoints';
import {IdNamed} from '../../../../types/Types';
import {
  domainModelsDeleteSuccess,
  domainModelsPostSuccess,
  domainModelsPutSuccess,
} from '../../../domain-models/domainModelsActions';
import {
  ADD_PARAMETER_TO_SELECTION,
  DESELECT_SELECTION,
  RESET_SELECTION,
  SELECT_SAVED_SELECTION,
  SET_SELECTION,
} from '../selectionActions';
import {ParameterName, SelectionParameter, UserSelection} from '../selectionModels';
import {initialState, selection} from '../selectionReducer';

describe('selectionReducer', () => {

  const mockPayload: UserSelection = {
    id: 5,
    name: 'something else',
    isChanged: false,
    selectionParameters: {
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
      const state: UserSelection = {...initialState};
      const stockholm: IdNamed = {id: 'sweden,stockholm', name: 'stockholm'};
      const selectionParameters: SelectionParameter = {
        ...stockholm,
        parameter: ParameterName.cities,
      };

      const expected: UserSelection = {
        ...initialState,
        isChanged: true,
        selectionParameters: {
          ...state.selectionParameters,
          cities: [stockholm.id],
        },
      };
      expect(selection(state, {type: ADD_PARAMETER_TO_SELECTION, payload: selectionParameters})).toEqual(expected);
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

      const intermediateState: UserSelection = selection(initialState, {
        type: ADD_PARAMETER_TO_SELECTION,
        payload: selectionParameterItem,
      });
      const finalState: UserSelection = selection(intermediateState, {
        type: ADD_PARAMETER_TO_SELECTION,
        payload: selectionParametersArray,
      });

      const expected: UserSelection = {
        ...intermediateState,
        isChanged: true,
        selectionParameters: {
          ...intermediateState.selectionParameters,
          cities: [gothenburg.id, stockholm.id, malmo.id],
        },
      };
      expect(finalState).toEqual(expected);
    });

    it('set filterParam as selected list', () => {

      const selectionParameterInitial: SelectionParameter = {parameter: ParameterName.cities, ...gothenburg};
      const selectionParametersFinal: SelectionParameter = {
        parameter: ParameterName.cities,
        ...stockholm,
      };

      const intermediateState: UserSelection = selection(initialState, {
          type: SET_SELECTION,
          payload: selectionParameterInitial,
        })
      ;
      const finalState: UserSelection = selection(intermediateState, {
          type: SET_SELECTION,
          payload: selectionParametersFinal,
        })
      ;

      const expected: UserSelection = {
        ...intermediateState,
        isChanged: true,
        selectionParameters: {
          ...intermediateState.selectionParameters,
          cities: [stockholm.id],
        },
      };
      expect(finalState).toEqual(expected);
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

      const intermediateState: UserSelection = selection(initialState, {
        type: SET_SELECTION,
        payload: selectionParameterInitial,
      });
      const finalState: UserSelection = selection(intermediateState, {
        type: SET_SELECTION,
        payload: selectionParametersFinal,
      });

      const expected: UserSelection = {
        ...intermediateState,
        isChanged: true,
        selectionParameters: {
          ...intermediateState.selectionParameters,
          cities: [stockholm.id, malmo.id],
        },
      };
      expect(finalState).toEqual(expected);
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

      const expected: UserSelection = {
        id: 5,
        name: 'something else',
        isChanged: true,
        selectionParameters: {cities: ['sweden,stockholm'], addresses: [1, 2, 3], period: Period.latest},
      };

      expect(state).toEqual(expected);
    });
  });

  describe('saveSelection', () => {

    it('selects newly created selection', () => {
      const newSelection: UserSelection = {
        id: 999,
        name: 'test 999',
        selectionParameters: {
          period: Period.currentMonth,
        },
        isChanged: false,
      };

      const state = selection(
        initialState,
        {type: domainModelsPostSuccess(EndPoints.userSelections), payload: newSelection},
      );
      expect(state).toEqual(newSelection);
    });

  });

  describe('updateSelection', () => {

    it('selects newly updated selection', () => {
      const newSelection: UserSelection = {
        id: 999,
        name: 'test 999',
        selectionParameters: {
          period: Period.currentMonth,
        },
        isChanged: false,
      };

      const state = selection(
        initialState,
        {type: domainModelsPutSuccess(EndPoints.userSelections), payload: newSelection},
      );
      expect(state).toEqual(newSelection);
    });

  });

  describe('deleteUserSelection', () => {

    it('resets to initial selection if currently selected selection is deleted', () => {
      const newSelection: UserSelection = {
        id: 999,
        name: 'test 999',
        selectionParameters: {
          period: Period.currentMonth,
        },
        isChanged: false,
      };

      const state = selection(
        newSelection,
        {type: domainModelsDeleteSuccess(EndPoints.userSelections), payload: newSelection},
      );
      expect(state).toEqual(initialState);
    });
  });

});

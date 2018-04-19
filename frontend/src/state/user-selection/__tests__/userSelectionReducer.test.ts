import {DateRange, Period} from '../../../components/dates/dateModels';
import {momentWithTimeZone} from '../../../helpers/dateHelpers';
import {EndPoints} from '../../../services/endPoints';
import {IdNamed} from '../../../types/Types';
import {
  domainModelsDeleteSuccess,
  domainModelsPostSuccess,
  domainModelsPutSuccess,
} from '../../domain-models/domainModelsActions';
import {
  ADD_PARAMETER_TO_SELECTION,
  DESELECT_SELECTION,
  RESET_SELECTION,
  SELECT_SAVED_SELECTION,
  SET_SELECTION, setCustomDateRange,
} from '../userSelectionActions';
import {ParameterName, SelectionParameter, UserSelection, UserSelectionState} from '../userSelectionModels';
import {initialState, userSelection} from '../userSelectionReducer';

describe('userSelectionReducer', () => {

  const mockPayload: UserSelection = {
    id: 5,
    name: 'something else',
    isChanged: false,
    selectionParameters: {
      cities: ['sweden,gothenburg', 'sweden,stockholm'],
      addresses: [1, 2, 3],
      dateRange: {period: Period.latest},
    },
  };

  const mockPayloadState: UserSelectionState = {
    userSelection: {...mockPayload},
  };

  const gothenburg: IdNamed = {id: 'sweden,gothenburg', name: 'gothenburg'};
  const stockholm: IdNamed = {id: 'sweden,stockholm', name: 'stockholm'};
  const malmo: IdNamed = {id: 'sweden,malmo', name: 'malmo'};

  describe('select saved selections', () => {

    it('replaces current selection', () => {
      const state: UserSelectionState = userSelection(
        initialState,
        {type: SELECT_SAVED_SELECTION, payload: mockPayload},
      );

      const expectedState: UserSelectionState = {userSelection: {...mockPayload, isChanged: false}};
      expect(state).toEqual(expectedState);
    });
  });

  describe('update current selection', () => {

    it('adds to selected list', () => {
      const state: UserSelectionState = {...initialState};
      const stockholm: IdNamed = {id: 'sweden,stockholm', name: 'stockholm'};
      const selectionParameters: SelectionParameter = {
        ...stockholm,
        parameter: ParameterName.cities,
      };

      const expected: UserSelectionState = {
        ...initialState,
        userSelection: {
          ...initialState.userSelection,
          isChanged: true,
          selectionParameters: {
            ...state.userSelection.selectionParameters,
            cities: [stockholm.id],
          },
        },
      };

      const actual: UserSelectionState = userSelection(
        state,
        {type: ADD_PARAMETER_TO_SELECTION, payload: selectionParameters},
      );
      expect(actual).toEqual(expected);
    });

    it('sets custom date range', () => {
      const initialState: UserSelectionState = userSelection(undefined, {type: 'whatever'});
      const start: Date = momentWithTimeZone('2018-12-09').toDate();
      const end: Date = momentWithTimeZone('2018-12-24').toDate();
      const dateRange: DateRange = {start, end};

      const expected: UserSelectionState = {
        userSelection: {
          ...initialState.userSelection,
          isChanged: true,
          selectionParameters: {
            ...initialState.userSelection.selectionParameters,
            dateRange: {period: Period.custom, customDateRange: {start, end}},
          },
        },
      };

      expect(userSelection(initialState, setCustomDateRange(dateRange))).toEqual(expected);
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

      const intermediateState: UserSelectionState = userSelection(initialState, {
        type: ADD_PARAMETER_TO_SELECTION,
        payload: selectionParameterItem,
      });
      const finalState: UserSelectionState = userSelection(intermediateState, {
        type: ADD_PARAMETER_TO_SELECTION,
        payload: selectionParametersArray,
      });

      const expected: UserSelectionState = {
        ...intermediateState,
        userSelection: {
          ...intermediateState.userSelection,
          isChanged: true,
          selectionParameters: {
            ...intermediateState.userSelection.selectionParameters,
            cities: [gothenburg.id, stockholm.id, malmo.id],
          },
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

      const intermediateState: UserSelectionState = userSelection(initialState, {
          type: SET_SELECTION,
          payload: selectionParameterInitial,
        })
      ;
      const finalState: UserSelectionState = userSelection(intermediateState, {
          type: SET_SELECTION,
          payload: selectionParametersFinal,
        })
      ;

      const expected: UserSelectionState = {
        ...intermediateState,
        userSelection: {
          ...intermediateState.userSelection,
          isChanged: true,
          selectionParameters: {
            ...intermediateState.userSelection.selectionParameters,
            cities: [stockholm.id],
          },
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

      const intermediateState: UserSelectionState = userSelection(initialState, {
        type: SET_SELECTION,
        payload: selectionParameterInitial,
      });
      const finalState: UserSelectionState = userSelection(intermediateState, {
        type: SET_SELECTION,
        payload: selectionParametersFinal,
      });

      const expected: UserSelectionState = {
        ...intermediateState,
        userSelection: {
          ...intermediateState.userSelection,
          isChanged: true,
          selectionParameters: {
            ...intermediateState.userSelection.selectionParameters,
            cities: [stockholm.id, malmo.id],
          },
        },
      };
      expect(finalState).toEqual(expected);
    });

  });

  describe('reset selection', () => {

    it('resets current selection', () => {
      let state: UserSelectionState = userSelection(initialState, {type: SELECT_SAVED_SELECTION, payload: mockPayload});

      expect(state).not.toEqual(initialState);

      state = userSelection(state, {type: RESET_SELECTION});

      expect(state).toEqual(initialState);
    });
  });

  describe('deselect', () => {

    it('will deselect selected city', () => {
      const parameter: SelectionParameter = {
        parameter: ParameterName.cities,
        ...gothenburg,
      };

      const state: UserSelectionState = userSelection(mockPayloadState, {type: DESELECT_SELECTION, payload: parameter});

      const expected: UserSelectionState = {
        userSelection: {
          id: 5,
          name: 'something else',
          isChanged: true,
          selectionParameters: {cities: ['sweden,stockholm'], addresses: [1, 2, 3], dateRange: {period: Period.latest}},
        },
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
          dateRange: {period: Period.currentMonth},
        },
        isChanged: false,
      };

      const state: UserSelectionState = userSelection(
        initialState,
        {type: domainModelsPostSuccess(EndPoints.userSelections), payload: newSelection},
      );

      const expected: UserSelectionState = {
        userSelection: {
          ...newSelection,
        },
      };

      expect(state).toEqual(expected);
    });

  });

  describe('updateSelection', () => {

    it('selects newly updated selection', () => {
      const newSelection: UserSelection = {
        id: 999,
        name: 'test 999',
        selectionParameters: {
          dateRange: {period: Period.currentMonth},
        },
        isChanged: false,
      };

      const state: UserSelectionState = userSelection(
        initialState,
        {type: domainModelsPutSuccess(EndPoints.userSelections), payload: newSelection},
      );

      const expected: UserSelectionState = {
        userSelection: {
          ...newSelection,
        },
      };

      expect(state).toEqual(expected);
    });

  });

  describe('deleteUserSelection', () => {

    it('resets to initial selection if currently selected selection is deleted', () => {
      const currentSelection: UserSelection = {
        id: 999,
        name: 'test 999',
        selectionParameters: {
          dateRange: {period: Period.currentMonth},
        },
        isChanged: false,
      };

      const currentState: UserSelectionState = {
        userSelection: {
          ...currentSelection,
        },
      };

      const state: UserSelectionState = userSelection(
        currentState,
        {type: domainModelsDeleteSuccess(EndPoints.userSelections), payload: currentSelection},
      );
      expect(state).toEqual(initialState);
    });
  });

});

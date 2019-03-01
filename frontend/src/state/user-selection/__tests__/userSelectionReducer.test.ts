import {DateRange, Period} from '../../../components/dates/dateModels';
import {momentAtUtcPlusOneFrom} from '../../../helpers/dateHelpers';
import {EndPoints} from '../../../services/endPoints';
import {IdNamed, toIdNamed} from '../../../types/Types';
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
  setCustomDateRange,
} from '../userSelectionActions';
import {ParameterName, SelectionParameter, UserSelection, UserSelectionState, } from '../userSelectionModels';
import {initialState, userSelection} from '../userSelectionReducer';

describe('userSelectionReducer', () => {

  const gothenburg: IdNamed = {id: 'sweden,gothenburg', name: 'gothenburg'};
  const stockholm: IdNamed = {id: 'sweden,stockholm', name: 'stockholm'};

  const mockPayload: UserSelection = {
    id: 5,
    name: 'something else',
    isChanged: false,
    selectionParameters: {
      cities: [{...gothenburg}, {...stockholm}],
      addresses: [toIdNamed('1'), toIdNamed('2'), toIdNamed('3')],
      dateRange: {period: Period.latest},
    },
  };

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
      const selectionParameters: SelectionParameter = {
        item: {...stockholm},
        parameter: ParameterName.cities,
      };

      const expected: UserSelectionState = {
        ...initialState,
        userSelection: {
          ...initialState.userSelection,
          isChanged: true,
          selectionParameters: {
            ...state.userSelection.selectionParameters,
            cities: [{...stockholm}],
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
      const start: Date = momentAtUtcPlusOneFrom('2018-12-09').toDate();
      const end: Date = momentAtUtcPlusOneFrom('2018-12-24').toDate();
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

  });

  describe('reset selection', () => {

    it('resets current selection', () => {
      let state: UserSelectionState = userSelection(
        initialState,
        {type: SELECT_SAVED_SELECTION, payload: mockPayload},
      );

      expect(state).not.toEqual(initialState);

      state = userSelection(state, {type: RESET_SELECTION});

      expect(state).toEqual(initialState);
    });
  });

  describe('deselect', () => {

    it('will deselect selected city', () => {
      const userSelectionState: UserSelectionState = {
        userSelection: {...mockPayload},
      };

      const payload: SelectionParameter = {
        parameter: ParameterName.cities,
        item: {...gothenburg},
      };

      const state: UserSelectionState = userSelection(
        userSelectionState,
        {type: DESELECT_SELECTION, payload},
      );

      const expected: UserSelectionState = {
        userSelection: {
          id: 5,
          name: 'something else',
          isChanged: true,
          selectionParameters: {
            cities: [stockholm],
            addresses: [toIdNamed('1'), toIdNamed('2'), toIdNamed('3')],
            dateRange: {period: Period.latest},
          },
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

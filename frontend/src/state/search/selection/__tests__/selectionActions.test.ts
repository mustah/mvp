import {routerActions} from 'react-router-redux';
import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {testData} from '../../../../__tests__/testDataFactory';
import {Period} from '../../../../components/dates/dateModels';
import {RootState} from '../../../../reducers/rootReducer';
import {IdNamed} from '../../../../types/Types';
import {UserSelectionState} from '../../searchParameterModels';
import {
  ADD_PARAMETER_TO_SELECTION,
  closeSelectionPage,
  DESELECT_SELECTION,
  SELECT_PERIOD,
  SELECT_SAVED_SELECTION,
  selectPeriod,
  selectSavedSelection,
  toggleParameterInSelection,
} from '../selectionActions';
import {ParameterName, SelectionParameter} from '../selectionModels';
import {initialState} from '../selectionReducer';

const configureMockStore = configureStore([thunk]);

describe('selectionActions', () => {

  const country = testData.selections.locations.countries[0];
  const gothenburg: IdNamed = {name: country.cities[0].name, id: 'got'};
  const stockholm: IdNamed = {name: country.cities[1].name, id: 'sto'};

  let store;

  beforeEach(() => {
    store = configureMockStore({});
  });

  const savedSelection21 = {
    ...initialState,
    id: 21,
    name: 'test 21',
  };

  const userSelection: UserSelectionState = {...initialState};
  const rootState = {
    userSelection,
    domainModels: {
      userSelections: {
        entities: {
          1: {
            ...initialState,
            id: 1,
            name: 'test 1',
          },
          21: {
            ...savedSelection21,
          },
        },
        result: [1, 21],
      },
    },
  };
  const rootStateNoSaved = {
    ...rootState,
    userSelection: {...rootState.userSelection},
    domainModels: {
      userSelections: {
        entities: {},
        result: [],
      },
    },
  };

  describe('close selection page', () => {

    it('closes selection page and navigates back to previous page', () => {
      store.dispatch(closeSelectionPage());

      expect(store.getActions()).toEqual([
        routerActions.goBack(),
      ]);
    });
  });

  describe('select from saved selections', () => {

    it('sets new selection', () => {
      store = configureMockStore(rootState);

      store.dispatch(selectSavedSelection(savedSelection21.id));

      expect(store.getActions()).toEqual([
        {type: SELECT_SAVED_SELECTION, payload: savedSelection21},
      ]);
    });

    it('does not dispatch if the selection cannot be found', () => {
      store = configureMockStore(rootState);

      store.dispatch(selectSavedSelection({
        ...initialState,
        id: 99,
        name: 'test 99',
      }.id));

      expect(store.getActions()).toEqual([]);
    });

  });

  describe('toggle selection', () => {

    it('set selection', async () => {
      const selection: IdNamed = {...gothenburg};
      const parameter: SelectionParameter = {...selection, parameter: ParameterName.cities};
      store = configureMockStore(rootStateNoSaved);

      store.dispatch(toggleParameterInSelection(parameter));

      expect(store.getActions()).toEqual([
        {type: ADD_PARAMETER_TO_SELECTION, payload: parameter},
      ]);
    });

    it('deselects selected city', () => {
      const stateWithSelection: Partial<RootState> = {
        userSelection: {
          userSelection: {
            ...initialState.userSelection,
            selectionParameters: {
              ...initialState.userSelection.selectionParameters,
              [ParameterName.cities]: [stockholm.id],
            },
          },
        },
      };
      store = configureMockStore(stateWithSelection);

      const payload: SelectionParameter = {...stockholm, parameter: ParameterName.cities};
      store.dispatch(toggleParameterInSelection(payload));

      expect(store.getActions()).toEqual([
        {type: DESELECT_SELECTION, payload},
      ]);
    });

    it('set several selections', () => {
      const p1: SelectionParameter = {...stockholm, parameter: ParameterName.cities};
      const p2: SelectionParameter = {...gothenburg, parameter: ParameterName.cities};
      store = configureMockStore(rootStateNoSaved);

      store.dispatch(toggleParameterInSelection(p1));
      store.dispatch(toggleParameterInSelection(p2));

      expect(store.getActions()).toEqual([
        {type: ADD_PARAMETER_TO_SELECTION, payload: p1},
        {type: ADD_PARAMETER_TO_SELECTION, payload: p2},
      ]);
    });

    it('select period', async () => {
      const period = Period.previousMonth;
      store = configureMockStore(rootStateNoSaved);

      store.dispatch(selectPeriod(period));

      expect(store.getActions()).toEqual([
        {type: SELECT_PERIOD, payload: period},
      ]);
    });
  });

});

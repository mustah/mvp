import {routerActions} from 'react-router-redux';
import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {testData} from '../../../../__tests__/testDataFactory';
import {Period} from '../../../../components/dates/dateModels';
import {IdNamed} from '../../../../types/Types';
import {
  ADD_SELECTION,
  CLOSE_SELECTION_PAGE,
  closeSelectionPage, DESELECT_SELECTION, SELECT_PERIOD, SELECT_SAVED_SELECTION,
  selectPeriod,
  selectSavedSelection, SET_SELECTION,
  setSelection,
  toggleSelection,
} from '../selectionActions';
import {ParameterName, SelectionParameter, SelectionState} from '../selectionModels';
import {initialState} from '../selectionReducer';

const configureMockStore = configureStore([thunk]);

describe('selectionActions', () => {

  const gothenburg: IdNamed = {...testData.selections.cities[0]};
  const stockholm: IdNamed = {...testData.selections.cities[1]};

  let store;

  beforeEach(() => {
    store = configureMockStore({});
  });

  const savedSelection21 = {
    ...initialState,
    id: 21,
    name: 'test 21',
  };
  const saved: SelectionState[] = [
    {
      ...initialState,
      id: 1,
      name: 'test 1',
    },
    savedSelection21,
  ];
  const rootState = {searchParameters: {selection: {...initialState}, saved}};
  const rootStateNoSaved = {...rootState, searchParameters: {...rootState.searchParameters, saved: []}};

  describe('close selection page', () => {

    it('closes selection page and navigates back to previous page', () => {
      store.dispatch(closeSelectionPage());

      expect(store.getActions()).toEqual([
        {type: CLOSE_SELECTION_PAGE},
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

      store.dispatch(toggleSelection(parameter));

      expect(store.getActions()).toEqual([
        {type: ADD_SELECTION, payload: parameter},
      ]);
    });

    it('deselects selected city', () => {
      const selection = {selected: {...initialState, [ParameterName.cities]: [stockholm.id]}};
      const stateWithSelection = {searchParameters: {selection, saved: []}};
      const payload: SelectionParameter = {...stockholm, parameter: ParameterName.cities};
      store = configureMockStore(stateWithSelection);

      store.dispatch(toggleSelection(payload));

      expect(store.getActions()).toEqual([
        {type: DESELECT_SELECTION, payload},
      ]);
    });

    it('set several selections', () => {
      const p1: SelectionParameter = {...stockholm, parameter: ParameterName.cities};
      const p2: SelectionParameter = {...gothenburg, parameter: ParameterName.cities};
      store = configureMockStore(rootStateNoSaved);

      store.dispatch(toggleSelection(p1));
      store.dispatch(toggleSelection(p2));

      expect(store.getActions()).toEqual([
        {type: ADD_SELECTION, payload: p1},
        {type: ADD_SELECTION, payload: p2},
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

  describe('set selection action', () => {
    it('set the selection of one parameter id', () => {
      const rootState = {searchParameters: {selection: {...initialState}, saved: []}};
      const payload: SelectionParameter = {parameter: ParameterName.cities, ...gothenburg};
      store = configureMockStore(rootState);

      store.dispatch(setSelection(payload));

      expect(store.getActions()).toEqual([
        {type: SET_SELECTION, payload},
      ]);
    });
  });
});

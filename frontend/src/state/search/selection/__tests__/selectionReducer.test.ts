import {normalize} from 'normalizr';
import {testData} from '../../../../__tests__/testDataFactory';
import {IdNamed} from '../../../../types/Types';
import {selectionsSchema} from '../../../domain-models/domainModelsSchemas';
import {
  addSelectionAction, closeSelectionPageAction, deselectSelection, resetSelectionAction,
  selectSavedSelectionAction, setSelectionAction,
} from '../selectionActions';
import {ParameterName, SelectionParameter, SelectionState} from '../selectionModels';
import {initialState, selection} from '../selectionReducer';

describe('selectionReducer', () => {

  const mockPayload: SelectionState = {
    id: 5,
    name: 'something else',
    isChanged: false,
    selected: {
      cities: ['got', 'sto'],
      addresses: [1, 2, 3],
    },
  };

  describe('normalize state', () => {

    it('normalized selection data', () => {
      const normalizedData = normalize(testData.selections, selectionsSchema);

      expect(normalizedData).toEqual({
        entities: {
          addresses: {
            1: {
              id: 1,
              name: 'Stampgatan 46',
              cityId: 'got',
            },
            2: {
              id: 2,
              name: 'Stampgatan 33',
              cityId: 'got',
            },
            3: {
              id: 3,
              name: 'Kungsgatan 44',
              cityId: 'sto',
            },
            4: {
              id: 4,
              name: 'Drottninggatan 1',
              cityId: 'mmx',
            },
            5: {
              id: 5,
              name: 'Åvägen 9',
              cityId: 'kub',
            },
          },
          cities: {
            got: {
              id: 'got',
              name: 'Göteborg',
            },
            kub: {
              id: 'kub',
              name: 'Kungsbacka',
            },
            mmx: {
              id: 'mmx',
              name: 'Malmö',
            },
            sto: {
              id: 'sto',
              name: 'Stockholm',
            },
          },
        },
        result: {
          addresses: [
            1,
            2,
            3,
            4,
            5,
          ],
          alarms: [],
          users: [],
          cities: [
            'got',
            'sto',
            'mmx',
            'kub',
          ],
          manufacturers: [],
          productModels: [],
          meterStatuses: [],
          gatewayStatuses: [],
        },
      });
    });
  });

  describe('select saved selections', () => {

    it('replaces current selection', () => {
      const state = selection(initialState, selectSavedSelectionAction(mockPayload));

      expect(state).toEqual({...mockPayload, isChanged: false});
    });
  });

  describe('update current selection', () => {

    it('adds to selected list', () => {
      const state = {...initialState};
      const stockholm: IdNamed = {...testData.selections.cities[0]};
      const selectionParameters: SelectionParameter = {
        ...stockholm,
        parameter: ParameterName.cities,
      };

      expect(selection(state, addSelectionAction(selectionParameters))).toEqual({
        ...initialState,
        isChanged: true,
        selected: {
          ...state.selected,
          cities: [stockholm.id],
        },
      });
    });

    it('adds array of filterParams to selected list', () => {
      const gothenburg: IdNamed = {...testData.selections.cities[0]};
      const stockholm: IdNamed = {...testData.selections.cities[1]};
      const malmo: IdNamed = {...testData.selections.cities[2]};

      const selectionParameterItem: SelectionParameter = {parameter: ParameterName.cities, ...gothenburg};
      const selectionParametersArray: SelectionParameter = {
        parameter: ParameterName.cities,
        id: [
          stockholm.id,
          malmo.id,
        ],
      };

      const intermediateState: SelectionState = selection(initialState, addSelectionAction(selectionParameterItem));
      const finalState: SelectionState = selection(intermediateState, addSelectionAction(selectionParametersArray));

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
      const gothenburg: IdNamed = {...testData.selections.cities[0]};
      const stockholm: IdNamed = {...testData.selections.cities[1]};

      const selectionParameterInitial: SelectionParameter = {parameter: ParameterName.cities, ...gothenburg};
      const selectionParametersFinal: SelectionParameter = {
        parameter: ParameterName.cities,
        ...stockholm,
      };

      const intermediateState: SelectionState = selection(initialState, setSelectionAction(selectionParameterInitial));
      const finalState: SelectionState = selection(intermediateState, setSelectionAction(selectionParametersFinal));

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
      const gothenburg: IdNamed = {...testData.selections.cities[0]};
      const stockholm: IdNamed = {...testData.selections.cities[1]};
      const malmo: IdNamed = {...testData.selections.cities[2]};

      const selectionParameterInitial: SelectionParameter = {parameter: ParameterName.cities, ...gothenburg};
      const selectionParametersFinal: SelectionParameter = {
        parameter: ParameterName.cities,
        id: [
          stockholm.id,
          malmo.id,
        ],
      };

      const intermediateState: SelectionState = selection(initialState, setSelectionAction(selectionParameterInitial));
      const finalState: SelectionState = selection(intermediateState, setSelectionAction(selectionParametersFinal));

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
      let state = selection(initialState, selectSavedSelectionAction(mockPayload));

      expect(state).not.toEqual(initialState);

      state = selection(state, resetSelectionAction());

      expect(state).toEqual(initialState);
    });
  });

  describe('deselect', () => {

    it('will deselect selected city', () => {
      const gothenburg: IdNamed = testData.selections.cities[0];
      const parameter: SelectionParameter = {
        parameter: ParameterName.cities,
        ...gothenburg,
      };

      const state = selection(mockPayload, deselectSelection(parameter));

      expect(state).toEqual({
        id: 5,
        name: 'something else',
        isChanged: true,
        selected: {cities: ['sto'], addresses: [1, 2, 3]},
      });
    });
  });

  describe('closeSelectionPage', () => {

    it('will mark selection state as not changed when the page is closed', () => {
      const state = selection({...mockPayload, isChanged: true}, closeSelectionPageAction());

      expect(state).toEqual({...mockPayload, isChanged: false});
    });

    it('will not toggle selection state is changed attribute when closing selection page', () => {
      const state = selection({...mockPayload, isChanged: false}, closeSelectionPageAction());

      expect(state).toEqual({...mockPayload, isChanged: false});
    });

  });

});

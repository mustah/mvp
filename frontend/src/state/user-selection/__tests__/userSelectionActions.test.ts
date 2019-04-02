import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {routes} from '../../../app/routes';
import {DateRange, Period} from '../../../components/dates/dateModels';
import {momentAtUtcPlusOneFrom} from '../../../helpers/dateHelpers';
import {RootState} from '../../../reducers/rootReducer';
import {EndPoints} from '../../../services/endPoints';
import {IdNamed, toIdNamed} from '../../../types/Types';
import {putRequestOf} from '../../domain-models/domainModelsActions';
import {mapSelectedIdToCity} from '../../domain-models/selections/selectionsApiActions';
import {Quantity} from '../../ui/graph/measurement/measurementModels';
import {
  addParameterToSelection,
  deselectSelection,
  resetSelection,
  selectPeriod,
  selectSavedSelection,
  selectSavedSelectionAction,
  selectSelection,
  setCustomDateRange,
  setThreshold,
  setThresholdAction,
  shouldMigrateSelectionParameters,
  toggleParameter,
} from '../userSelectionActions';
import {
  initialSelectionId,
  OldSelectionParameters,
  ParameterName,
  RelationalOperator,
  SelectedParameters,
  SelectionParameter,
  ThresholdQuery,
  UserSelection,
  UserSelectionState,
} from '../userSelectionModels';
import {initialState} from '../userSelectionReducer';

const configureMockStore = configureStore([thunk]);

describe('userSelectionActions', () => {

  const gothenburg: IdNamed = {name: 'göteborg', id: 'got'};
  const stockholm: IdNamed = {name: 'stockholm', id: 'sto'};

  let store;

  beforeEach(() => {
    store = configureMockStore({});
  });

  const oldUserSelectionWithGasType = {
    ...initialState.userSelection,
    selectionParameters: {
      ...initialState.userSelection.selectionParameters,
      media: ['Gas'],
    },
    id: 33,
    name: 'old media',
  };

  const rootState = {
    userSelection: {...initialState},
    domainModels: {
      userSelections: {
        entities: {
          1: {
            ...initialState.userSelection,
            id: 1,
            name: 'test 1',
          },
          21: {
            ...initialState.userSelection,
            id: 21,
            name: 'test 21',
          },
          33: oldUserSelectionWithGasType,
        },
        result: [1, 21, 33],
      },
    },
    previousSession: {},
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

  describe('setCustomDateRange', () => {
    it('sends out a request to set a new customDateRange', () => {
      const start: Date = momentAtUtcPlusOneFrom('2018-12-09').toDate();
      const end: Date = momentAtUtcPlusOneFrom('2018-12-24').toDate();
      const dateRange: DateRange = {start, end};
      store.dispatch(setCustomDateRange(dateRange));

      expect(store.getActions()).toEqual([setCustomDateRange({start, end})]);
    });
  });

  describe('selectSavedSelection', () => {

    it('sets new selection', () => {
      store = configureMockStore(rootState);

      store.dispatch(selectSavedSelection(21));

      const payload: UserSelection = {...initialState.userSelection, id: 21, name: 'test 21'};

      expect(store.getActions()).toEqual([selectSavedSelectionAction(payload)]);
    });

    it('set new selection and migrates its state when old state is stored', () => {
      store = configureMockStore(rootState);

      store.dispatch(selectSavedSelection(33));

      const payload = {...oldUserSelectionWithGasType};

      expect(store.getActions()).toEqual([
        {
          ...putRequestOf<UserSelection>(EndPoints.userSelections).request(),
        },
        selectSavedSelectionAction({
          ...payload,
          selectionParameters: {
            ...payload.selectionParameters,
            media: [{...toIdNamed('Gas')}],
          },
        }),
      ]);
    });

    it('does not dispatch if the selection cannot be found', () => {
      store = configureMockStore(rootState);

      store.dispatch(selectSavedSelection(-99));

      expect(store.getActions()).toEqual([]);
    });

    it('does not dispatch if already selected selection is selected', () => {
      const rootStateWithSelectedSelection = {
        ...rootState,
        userSelection: {
          ...initialState,
          id: 42,
        },
      };
      store = configureMockStore(rootStateWithSelectedSelection);

      store.dispatch(selectSavedSelection(42));
      expect(store.getActions()).toEqual([]);
    });

    describe('selectSelection', () => {

      beforeEach(() => {
        const rootStateWithSelectedSelection = {
          ...rootState,
          routing: {location: {pathname: routes.collection}},
        };
        store = configureMockStore(rootStateWithSelectedSelection);
      });

      describe('resets selection', () => {

        it('when no selectionId exists', () => {
          store.dispatch(selectSelection());

          expect(store.getActions()).toEqual([resetSelection()]);
        });

        it('when selectionId is initialSelectionId', () => {
          store.dispatch(selectSelection(initialSelectionId));

          expect(store.getActions()).toEqual([resetSelection()]);
        });
      });

      describe('selects selection', () => {

        it('select saved selection with id', () => {
          store.dispatch(selectSelection(21));

          const payload: UserSelection = {...initialState.userSelection, id: 21, name: 'test 21'};
          expect(store.getActions()).toEqual([selectSavedSelectionAction(payload)]);
        });
      });

    });

    describe('shouldMigrateSelectionParameters', () => {

      const oldSelectedParameters: OldSelectionParameters = {
        addresses: [],
        cities: [],
        facilities: [],
        gatewaySerials: [],
        media: [],
        secondaryAddresses: [],
      };

      describe('new selection parameters', () => {

        it('should not migrate initial selection parameters', () => {
          const selectionParameters = initialState.userSelection.selectionParameters;
          expect(shouldMigrateSelectionParameters(selectionParameters)).toBe(false);
        });

        it('should not migrate selection parameters as objects', () => {
          const selected: SelectedParameters = {
            ...initialState.userSelection.selectionParameters,
            media: [{...toIdNamed('Gas')}],
          };
          expect(shouldMigrateSelectionParameters(selected)).toBe(false);
        });

        it('should not migrate city selection parameters as objects', () => {
          const selected: SelectedParameters = {
            ...initialState.userSelection.selectionParameters,
            cities: [{...mapSelectedIdToCity('norge,olso')}],
          };
          expect(shouldMigrateSelectionParameters(selected)).toBe(false);
        });

      });

      describe('old selection parameters', () => {

        it('should migrate selection parameters with media type as ids only', () => {
          const parameters: OldSelectionParameters = {
            ...oldSelectedParameters,
            media: ['Gas'],
          };
          expect(shouldMigrateSelectionParameters(parameters)).toBe(true);
        });

        it('should migrate selection parameters with city as ids only', () => {
          const parameters: OldSelectionParameters = {
            ...oldSelectedParameters,
            cities: ['sweden,stockholm'],
          };
          expect(shouldMigrateSelectionParameters(parameters)).toBe(true);
        });

        it('should migrate selection parameters with address as ids only', () => {
          const parameters: OldSelectionParameters = {
            ...oldSelectedParameters,
            cities: ['sweden,stockholm,kungsgatan 18'],
          };
          expect(shouldMigrateSelectionParameters(parameters)).toBe(true);
        });
      });

    });

  });

  describe('toggleParameter', () => {

    describe('toggle cities selection', () => {

      it('deselects selected city', () => {
        const stateWithSelection: Partial<RootState> = {
          userSelection: {
            userSelection: {
              ...initialState.userSelection,
              selectionParameters: {
                ...initialState.userSelection.selectionParameters,
                [ParameterName.cities]: [stockholm],
              },
            },
          },
        };
        store = configureMockStore(stateWithSelection);

        const payload: SelectionParameter = {item: {...stockholm}, parameter: ParameterName.cities};
        store.dispatch(toggleParameter(payload));

        expect(store.getActions()).toEqual([deselectSelection(payload)]);
      });

      it('set selection', async () => {
        const selection: IdNamed = {...gothenburg};
        const parameter: SelectionParameter = {item: {...selection}, parameter: ParameterName.cities};
        store = configureMockStore(rootStateNoSaved);

        store.dispatch(toggleParameter(parameter));

        expect(store.getActions()).toEqual([addParameterToSelection(parameter)]);
      });

      it('set several selections', () => {
        const p1: SelectionParameter = {item: {...stockholm}, parameter: ParameterName.cities};
        const p2: SelectionParameter = {item: {...gothenburg}, parameter: ParameterName.cities};
        store = configureMockStore(rootStateNoSaved);

        store.dispatch(toggleParameter(p1));
        store.dispatch(toggleParameter(p2));

        expect(store.getActions()).toEqual([
          addParameterToSelection(p1),
          addParameterToSelection(p2),
        ]);
      });

    });

    describe('toggle gateway serials', () => {

      it('adds non-existing gateways serials', () => {
        const p1: SelectionParameter = {
          item: {id: '123', name: '123'},
          parameter: ParameterName.gatewaySerials,
        };
        const p2: SelectionParameter = {
          item: {id: 'abc', name: 'abc'},
          parameter: ParameterName.gatewaySerials,
        };
        store = configureMockStore(rootStateNoSaved);

        store.dispatch(toggleParameter(p1));
        store.dispatch(toggleParameter(p2));

        expect(store.getActions()).toEqual([
          addParameterToSelection(p1),
          addParameterToSelection(p2),
        ]);
      });

      it('adds selected parameter when selected gateway serials are undefined', () => {
        const oldRootState = {...rootState};
        delete oldRootState.userSelection.userSelection.selectionParameters[ParameterName.gatewaySerials];

        const p1: SelectionParameter = {
          item: {id: '123', name: '123'},
          parameter: ParameterName.gatewaySerials,
        };
        store = configureMockStore(oldRootState);

        store.dispatch(toggleParameter(p1));

        expect(store.getActions()).toEqual([addParameterToSelection(p1)]);
      });

    });

  });

  describe('selectPeriod', () => {

    it('selectsPeriod', async () => {
      const period = Period.previousMonth;
      store = configureMockStore(rootStateNoSaved);

      store.dispatch(selectPeriod(period));

      expect(store.getActions()).toEqual([selectPeriod(period)]);
    });
  });

  describe('setThreshold', () => {

    type IncompleteThresholdQuery =
      Partial<{ [key in keyof ThresholdQuery]: string | undefined | ThresholdQuery[key] }>;
    type UsersInput = ThresholdQuery | undefined | IncompleteThresholdQuery;

    const userSelectionStateFromThreshold = (threshold: ThresholdQuery): UserSelectionState => ({
      ...rootStateNoSaved.userSelection,
      userSelection: {
        ...rootStateNoSaved.userSelection.userSelection,
        selectionParameters: {
          ...rootStateNoSaved.userSelection.userSelection.selectionParameters,
          threshold,
        }
      }
    });

    const empty = undefined;
    const ok: ThresholdQuery = {
      value: '2',
      unit: 'kW',
      quantity: Quantity.power,
      relationalOperator: '>=' as RelationalOperator,
      dateRange: {
        period: Period.latest,
      },
    };
    const anotherOk: ThresholdQuery = {...ok, value: '3', duration: '2'};
    const incomplete: IncompleteThresholdQuery = {
      value: '',
      unit: undefined,
      quantity: undefined,
      relationalOperator: '>=' as RelationalOperator,
    };
    const anotherIncomplete: IncompleteThresholdQuery = {...incomplete, value: '3', duration: '2'};

    const triggersChange: boolean = true;
    const skipsAction: boolean = false;

    const testCases: Array<[number, UsersInput, UsersInput, boolean]> = [
      [1, empty, ok, triggersChange],
      [2, empty, incomplete, skipsAction],
      [3, empty, empty, skipsAction],

      [4, ok, anotherOk, triggersChange],
      [5, ok, incomplete, triggersChange],
      [6, ok, empty, triggersChange],
      [7, ok, ok, skipsAction],

      [8, incomplete, ok, triggersChange],
      [9, incomplete, anotherIncomplete, skipsAction],
      [10, incomplete, empty, skipsAction],
    ];

    test.each(testCases)(
      'test #%i: old state of %p and action of (%p) triggers? %p',
      (_, currentThresholdQuery, newThreshold, shouldTriggerAction) => {
        const currentState: UserSelectionState = userSelectionStateFromThreshold(currentThresholdQuery);
        store = configureMockStore({...rootStateNoSaved, userSelection: currentState});

        store.dispatch(setThreshold(newThreshold));

        expect(store.getActions()).toEqual(
          shouldTriggerAction
            ? [setThresholdAction(newThreshold)]
            : []
        );
      }
    );
  });

});

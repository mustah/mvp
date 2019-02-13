import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {TemporalResolution} from '../../../components/dates/dateModels';
import {idGenerator} from '../../../helpers/idGenerator';
import {initTranslations} from '../../../i18n/__tests__/i18nMock';
import {RootState} from '../../../reducers/rootReducer';
import {Medium, Quantity} from '../../../state/ui/graph/measurement/measurementModels';
import {SHOW_FAIL_MESSAGE} from '../../../state/ui/message/messageActions';
import {UiState} from '../../../state/ui/uiReducer';
import {uuid} from '../../../types/Types';
import {
  addToReport,
  hideAllLines,
  limit,
  selectEntryAdd,
  SET_SELECTED_ENTRIES,
  showMetersInGraph,
  toggleGroupItems,
  toggleSingleEntry,
} from '../reportActions';

type RelevantStateForSelectedItems = Pick<RootState, 'report' | 'selectionTree'> & {ui: Pick<UiState, 'indicator'>} ;

const configureMockStore: (state: RelevantStateForSelectedItems) => any = configureStore([thunk]);

describe('reportActions', () => {

  let initialState: RelevantStateForSelectedItems;

  beforeEach(() => {
    initialState = {
      report: {
        selectedListItems: [22, 'sweden,höganäs,hasselgatan 4'],
        hiddenLines: [],
        resolution: TemporalResolution.day,
      },
      selectionTree: {
        isFetching: false,
        isSuccessfullyFetched: true,
        entities: {
          cities: {
            'sweden,höganäs': {
              id: 'sweden,höganäs',
              city: 'sweden,höganäs',
              medium: [Medium.gas, Medium.water, Medium.districtHeating],
              name: 'höganäs',
              addresses: [
                'sweden,höganäs,hasselgatan 4',
                'sweden,höganäs,storgatan 5',
                'sweden,höganäs,väpnaregatan 10',
                'sweden,höganäs,lillgatan 22',
              ],
            },
          },
          addresses: {
            'sweden,höganäs,hasselgatan 4': {
              address: 'hasselgatan 4',
              city: 'sweden,höganäs',
              name: 'hasselgatan 4',
              meters: [22],
              id: 'sweden,höganäs,hasselgatan 4',
            },
            'sweden,höganäs,storgatan 5': {
              address: 'storgatan 5',
              city: 'sweden,höganäs',
              name: 'storgatan 5',
              meters: [33],
              id: 'sweden,höganäs,storgatan 5',
            },
            'sweden,höganäs,väpnaregatan 10': {
              address: 'väpnaregatan 10',
              city: 'sweden,höganäs',
              name: 'väpnaregatan 10',
              meters: [44],
              id: 'sweden,höganäs,väpnaregatan 10',
            },
            'sweden,höganäs,lillgatan 22': {
              address: 'lillgatan 22',
              city: 'sweden,höganäs',
              name: 'lillgatan 22',
              meters: [55],
              id: 'sweden,höganäs,lillgatan 22',
            },
          },
          meters: {
            22: {
              address: 'hasselgatan 4',
              city: 'sweden,höganäs',
              id: 22,
              name: '3000',
              medium: Medium.gas,
            },
            33: {
              address: 'storgatan 5',
              city: 'sweden,höganäs',
              id: 33,
              name: '3001',
              medium: Medium.gas,
            },
            44: {
              address: 'väpnaregatan 10',
              city: 'sweden,höganäs',
              id: 44,
              name: '3002',
              medium: Medium.water,
            },
            55: {
              address: 'lillgatan 22',
              city: 'sweden,höganäs',
              id: 55,
              name: '3003',
              medium: Medium.districtHeating,
            },
            999: {
              address: 'storgatan 12',
              city: 'sweden,kungbacka',
              id: 999,
              name: '999-unknown',
              medium: Medium.unknown,
            },
          },
        },
        result: {
          cities: [
            'sweden,höganäs',
          ],
        },
      },
      ui: {
        indicator: {
          selectedIndicators: {report: []},
          selectedQuantities: []
        },
      }
    };
  });

  describe('toggleSingleEntry', () => {

    describe('includes indicators representing selected items', () => {

      it('deselects all indicators for empty list of meters', () => {
        const store = configureMockStore({
          ...initialState,
          report: {
            selectedListItems: [44],
            hiddenLines: [],
            resolution: TemporalResolution.day,
          },
        });

        store.dispatch(toggleSingleEntry(44));

        const actions = store.getActions();
        expect(actions).toHaveLength(1);

        const {type, payload: {indicatorsToSelect, quantitiesToSelect}} = actions[0];
        expect(type).toEqual(SET_SELECTED_ENTRIES);
        expect(indicatorsToSelect).toEqual([]);
        expect(quantitiesToSelect).toEqual([]);
      });

      it('selects indicator when adding a single meter', () => {
        const store = configureMockStore({
          ...initialState,
          report: {
            selectedListItems: [],
            hiddenLines: [],
            resolution: TemporalResolution.day,
          },
        });

        store.dispatch(toggleSingleEntry(44));

        const actions = store.getActions();
        expect(actions).toHaveLength(1);

        const {type, payload: {indicatorsToSelect, quantitiesToSelect}} = actions[0];
        expect(type).toEqual(SET_SELECTED_ENTRIES);
        expect(indicatorsToSelect).toEqual([Medium.water]);
        expect(quantitiesToSelect).toEqual([Quantity.volume]);
      });

      it('selects indicators when adding a second meter with different medium', () => {
        const store = configureMockStore({
          ...initialState,
          report: {
            selectedListItems: [44],
            hiddenLines: [],
            resolution: TemporalResolution.day,
          },
        });

        store.dispatch(toggleSingleEntry(33));

        const actions = store.getActions();
        expect(actions).toHaveLength(1);

        const {type, payload: {indicatorsToSelect, quantitiesToSelect}} = actions[0];
        expect(type).toEqual(SET_SELECTED_ENTRIES);
        expect(indicatorsToSelect).toEqual([Medium.gas, Medium.water]);
        expect(quantitiesToSelect).toEqual([Quantity.volume]);
      });

      it('does not touch the indicators when adding a third meter with different medium', () => {
        const store = configureMockStore({
          ...initialState,
          report: {
            selectedListItems: [44, 33],
            hiddenLines: [],
            resolution: TemporalResolution.day,
          },
        });

        store.dispatch(toggleSingleEntry(55));

        const actions = store.getActions();
        expect(actions).toHaveLength(1);

        const {type, payload: {indicatorsToSelect, quantitiesToSelect}} = actions[0];
        expect(type).toEqual(SET_SELECTED_ENTRIES);
        expect(indicatorsToSelect).toEqual([Medium.gas, Medium.water]);
        expect(quantitiesToSelect).toEqual([Quantity.volume]);
      });

      it('uses the selected quantities from previous state', () => {
        const selectedListItems: uuid[] = [44, 33];
        const store = configureMockStore({
          ...initialState,
          report: {
            selectedListItems,
            hiddenLines: [],
            resolution: TemporalResolution.day,
          },
          ui: selectedUiQuantitiesFrom([Quantity.flow])
        });

        store.dispatch(toggleSingleEntry(55));

        expect(store.getActions()).toEqual([
          {
            type: SET_SELECTED_ENTRIES,
            payload: {
              ids: [...selectedListItems, 55],
              indicatorsToSelect: [Medium.gas, Medium.water],
              quantitiesToSelect: [Quantity.flow]
            }
          }
        ]);
      });

      it('prioritizes previous indicators when some meters are removed and some added', () => {
        const store = configureMockStore({
          ...initialState,
          report: {
            selectedListItems: [44, 33],
            hiddenLines: [],
            resolution: TemporalResolution.day,
          },
        });

        store.dispatch(toggleSingleEntry(55));

        const actions = store.getActions();
        expect(actions).toHaveLength(1);

        const {type, payload: {indicatorsToSelect, quantitiesToSelect}} = actions[0];
        expect(type).toEqual(SET_SELECTED_ENTRIES);
        expect(indicatorsToSelect).toEqual([Medium.gas, Medium.water]);
        expect(quantitiesToSelect).toEqual([Quantity.volume]);
      });

      it('adds the first two, prioritized, media when adding a city with three media', () => {
        const store = configureMockStore({
          ...initialState,
          report: {
            selectedListItems: [],
            hiddenLines: [],
            resolution: TemporalResolution.day,
          },
        });

        store.dispatch(toggleSingleEntry('sweden,höganäs'));

        const actions = store.getActions();
        expect(actions).toHaveLength(1);

        const {type, payload: {indicatorsToSelect, quantitiesToSelect}} = actions[0];
        expect(type).toEqual(SET_SELECTED_ENTRIES);
        expect(indicatorsToSelect).toEqual([Medium.districtHeating, Medium.gas]);
        expect(quantitiesToSelect).toEqual([Quantity.energy, Quantity.volume]);
      });

    });

    it('dispatches action to ADD id to selected entries if not already selected', () => {
      const store = configureMockStore(initialState);

      store.dispatch(toggleSingleEntry(44));

      const actions = store.getActions();
      expect(actions).toHaveLength(1);

      const {type, payload: {ids}} = actions[0];
      expect(type).toEqual(SET_SELECTED_ENTRIES);
      expect(ids).toEqual([22, 'sweden,höganäs,hasselgatan 4', 44]);
    });

    it('dispatches action to REMOVE id from selected entries if already selected', () => {
      const store = configureMockStore(initialState);

      store.dispatch(toggleSingleEntry(22));

      const actions = store.getActions();
      expect(actions).toHaveLength(1);

      const {type, payload: {ids}} = actions[0];
      expect(type).toEqual(SET_SELECTED_ENTRIES);
      expect(ids).toContain('sweden,höganäs,hasselgatan 4');
      expect(ids).not.toContain(22);
    });

  });

  describe('addToReport', () => {

    it('adds a meter that is not already selected', () => {
      const store = configureMockStore(initialState);

      store.dispatch(addToReport(33));

      const actions = store.getActions();
      expect(actions).toHaveLength(1);

      const {type, payload: {ids}} = actions[0];
      expect(type).toEqual(SET_SELECTED_ENTRIES);
      expect(ids).toContain(33);
    });

    it('does not fire an event if meter is already selected', () => {
      const store = configureMockStore(initialState);

      store.dispatch(addToReport(22));

      expect(store.getActions()).toHaveLength(0);
    });

    it('selects report indicators', () => {
      const store = configureMockStore({
        ...initialState,
        report: {
          selectedListItems: [],
          hiddenLines: [],
          resolution: TemporalResolution.day,
        },
      });

      store.dispatch(addToReport(44));

      const actions = store.getActions();
      expect(actions).toHaveLength(1);

      const {type, payload: {indicatorsToSelect, quantitiesToSelect}} = actions[0];
      expect(type).toEqual(SET_SELECTED_ENTRIES);
      expect(indicatorsToSelect).toEqual([Medium.water]);
      expect(quantitiesToSelect).toEqual([Quantity.volume]);
    });

    it('does not add unknown medium meter to graph', () => {
      const store = configureMockStore({...initialState});

      store.dispatch(addToReport(999));

      expect(store.getActions()).toEqual([]);
    });

  });

  describe('toggleGroupItems', () => {

    it('selects given address and meters, if address not already selected', () => {
      const store = configureMockStore(initialState);

      store.dispatch(toggleGroupItems('sweden,höganäs,storgatan 5'));

      const actions = store.getActions();
      expect(actions).toHaveLength(1);

      const {type, payload: {ids}} = actions[0];
      expect(type).toEqual(SET_SELECTED_ENTRIES);
      expect(ids).toContain('sweden,höganäs,storgatan 5');
      expect(ids).toContain(33);
    });

    it('deselects given address and meters, if address already selected', () => {
      const store = configureMockStore(initialState);

      store.dispatch(toggleGroupItems('sweden,höganäs,hasselgatan 4'));

      const actions = store.getActions();
      expect(actions).toHaveLength(1);

      const {type, payload: {ids}} = actions[0];
      expect(type).toEqual(SET_SELECTED_ENTRIES);
      expect(ids).toHaveLength(0);
    });

    describe('enforces a limit of 20 report meters/cities at a time', () => {

      const translations = {
        code: 'en',
        translation: {
          'only {{limit}} meters can be selected at the same time':
            'only {{limit}} meters can be selected at the same time',
        },
      };
      initTranslations(translations);

      it('shows failure message if trying to select more than num limit of meters at a time', () => {
        const address = 'storgatan 5';
        const city = 'sweden,höganäs';
        const addressId = `${city},${address}`;

        const state = {...initialState};
        state.selectionTree.entities.addresses[addressId] = {
          address,
          city,
          meters: [],
          name: address,
          id: addressId,
        };

        for (let i = 0; i < limit + 10; i++) {
          const id = idGenerator.uuid();
          state.selectionTree.entities.meters[id] = {
            address,
            city,
            id,
            name: `meter-${i}`,
            medium: Medium.gas,
          };

          state.selectionTree.entities.addresses[addressId].meters.push(id);
        }

        const store = configureMockStore(state);
        store.dispatch(toggleGroupItems(addressId));

        const actions = store.getActions();
        expect(actions).toHaveLength(1);

        const {type, payload} = actions[0];
        expect(type).toEqual(SHOW_FAIL_MESSAGE);
        expect(payload).toEqual(`Only ${limit} meters can be selected at the same time`);
      });

    });

  });

  describe('selectEntryAdd', () => {

    it('adds new id to selected', () => {
      const store = configureMockStore(initialState);

      store.dispatch(selectEntryAdd(33));

      expect(store.getActions()).toEqual([
        {
          type: SET_SELECTED_ENTRIES,
          payload: {
            ids: [...initialState.report.selectedListItems, 33],
            indicatorsToSelect: [Medium.gas],
            quantitiesToSelect: [Quantity.volume]
          }
        }
      ]);
    });

    it('adds new id to selected with already selected non-default quantity', () => {
      const store = configureMockStore({
        ...initialState,
        ui: selectedUiQuantitiesFrom([Quantity.flow]),
      });

      store.dispatch(selectEntryAdd(33));

      expect(store.getActions()).toEqual([
        {
          type: SET_SELECTED_ENTRIES,
          payload: {
            ids: [...initialState.report.selectedListItems, 33],
            indicatorsToSelect: [Medium.gas],
            quantitiesToSelect: [Quantity.flow]
          }
        }
      ]);
    });

    it('does not dispatch when id already exist in selected', () => {
      const store = configureMockStore(initialState);

      store.dispatch(selectEntryAdd(22));

      expect(store.getActions()).toHaveLength(0);
    });

  });

  describe('showMetersInGraph', () => {

    it('shows all meters in graph', () => {
      const store = configureMockStore({...initialState});
      const ids: uuid[] = [33, 22];

      store.dispatch(showMetersInGraph(ids));

      expect(store.getActions()).toEqual([
        {
          type: SET_SELECTED_ENTRIES,
          payload: {
            ids: [33, 22],
            indicatorsToSelect: [Medium.gas],
            quantitiesToSelect: [Quantity.volume]
          }
        }
      ]);
    });

    it('excludes meters with unknown medium', () => {
      const store = configureMockStore({...initialState});
      const ids: uuid[] = [999, 22, 33];

      store.dispatch(showMetersInGraph(ids));

      expect(store.getActions()).toEqual([
        {
          type: SET_SELECTED_ENTRIES,
          payload: {
            ids: [22, 33],
            indicatorsToSelect: [Medium.gas],
            quantitiesToSelect: [Quantity.volume]
          }
        }
      ]);
    });

    it('duplicate meter ids are removed', () => {
      const store = configureMockStore({...initialState});
      const ids: uuid[] = [33, 22, 33, 'sweden,höganäs,hasselgatan 4'];

      store.dispatch(showMetersInGraph(ids));

      expect(store.getActions()).toEqual([
        {
          type: SET_SELECTED_ENTRIES,
          payload: {
            ids: [33, 22],
            indicatorsToSelect: [Medium.gas],
            quantitiesToSelect: [Quantity.volume]
          }
        }
      ]);
    });

  });

  describe('hideAllLines', () => {

    it('dispatches hide all lines action creator', () => {
      const store = configureMockStore(initialState);

      store.dispatch(hideAllLines());

      expect(store.getActions()).toEqual([hideAllLines()]);
    });
  });

  const selectedUiQuantitiesFrom = (selectedQuantities: Quantity[]): Pick<UiState, 'indicator'> => ({
    ...initialState.ui,
    indicator: {
      ...initialState.ui.indicator,
      selectedQuantities
    }
  });
});

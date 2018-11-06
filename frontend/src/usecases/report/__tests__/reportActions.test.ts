import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {idGenerator} from '../../../helpers/idGenerator';
import {initTranslations} from '../../../i18n/__tests__/i18nMock';
import {RootState} from '../../../reducers/rootReducer';
import {Medium, Quantity} from '../../../state/ui/graph/measurement/measurementModels';
import {SHOW_FAIL_MESSAGE} from '../../../state/ui/message/messageActions';
import {UiState} from '../../../state/ui/uiReducer';
import {uuid} from '../../../types/Types';
import {
  addToReport,
  selectEntryAdd,
  SET_SELECTED_ENTRIES,
  toggleIncludingChildren,
  toggleSingleEntry,
} from '../reportActions';

type RelevantStateForSelectedItems = Pick<RootState, 'report' | 'selectionTree'> & {ui: Pick<UiState, 'indicator'>} ;

const configureMockStore: (state: RelevantStateForSelectedItems) => any = configureStore([thunk]);

describe('reportActions', () => {

  let initialState: RelevantStateForSelectedItems;

  beforeEach(() => {
    initialState = {
      report: {
        selectedListItems: [
          '905a785e-f215-4eb8-b31c-0a00a365a124',
          'sweden,höganäs,hasselgatan 4',
        ],
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
              meters: [
                '905a785e-f215-4eb8-b31c-0a00a365a124',
              ],
              id: 'sweden,höganäs,hasselgatan 4',
            },
            'sweden,höganäs,storgatan 5': {
              address: 'storgatan 5',
              city: 'sweden,höganäs',
              name: 'storgatan 5',
              meters: [
                '22b8fd17-fd83-469e-b0ca-4ab3808beebb',
              ],
              id: 'sweden,höganäs,storgatan 5',
            },
            'sweden,höganäs,väpnaregatan 10': {
              address: 'väpnaregatan 10',
              city: 'sweden,höganäs',
              name: 'väpnaregatan 10',
              meters: [
                '54c58358-9631-4de3-b76c-f018fbf0fc8b',
              ],
              id: 'sweden,höganäs,väpnaregatan 10',
            },
            'sweden,höganäs,lillgatan 22': {
              address: 'lillgatan 22',
              city: 'sweden,höganäs',
              name: 'lillgatan 22',
              meters: [
                '9ac413ed-ba1f-48d5-9793-7e259841595f',
              ],
              id: 'sweden,höganäs,lillgatan 22',
            },
          },
          meters: {
            '905a785e-f215-4eb8-b31c-0a00a365a124': {
              address: 'hasselgatan 4',
              city: 'sweden,höganäs',
              id: '905a785e-f215-4eb8-b31c-0a00a365a124',
              name: '3000',
              medium: Medium.gas,
            },
            '22b8fd17-fd83-469e-b0ca-4ab3808beebb': {
              address: 'storgatan 5',
              city: 'sweden,höganäs',
              id: '22b8fd17-fd83-469e-b0ca-4ab3808beebb',
              name: '3001',
              medium: Medium.gas,
            },
            '54c58358-9631-4de3-b76c-f018fbf0fc8b': {
              address: 'väpnaregatan 10',
              city: 'sweden,höganäs',
              id: '54c58358-9631-4de3-b76c-f018fbf0fc8b',
              name: '3002',
              medium: Medium.water,
            },
            '9ac413ed-ba1f-48d5-9793-7e259841595f': {
              address: 'lillgatan 22',
              city: 'sweden,höganäs',
              id: '9ac413ed-ba1f-48d5-9793-7e259841595f',
              name: '3003',
              medium: Medium.districtHeating,
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
            selectedListItems: ['54c58358-9631-4de3-b76c-f018fbf0fc8b'],
          },
        });

        store.dispatch(toggleSingleEntry('54c58358-9631-4de3-b76c-f018fbf0fc8b'));

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
          },
        });

        store.dispatch(toggleSingleEntry('54c58358-9631-4de3-b76c-f018fbf0fc8b'));

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
            selectedListItems: ['54c58358-9631-4de3-b76c-f018fbf0fc8b'],
          },
        });

        store.dispatch(toggleSingleEntry('22b8fd17-fd83-469e-b0ca-4ab3808beebb'));

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
            selectedListItems: [
              '54c58358-9631-4de3-b76c-f018fbf0fc8b',
              '22b8fd17-fd83-469e-b0ca-4ab3808beebb',
            ],
          },
        });

        store.dispatch(toggleSingleEntry('9ac413ed-ba1f-48d5-9793-7e259841595f'));

        const actions = store.getActions();
        expect(actions).toHaveLength(1);

        const {type, payload: {indicatorsToSelect, quantitiesToSelect}} = actions[0];
        expect(type).toEqual(SET_SELECTED_ENTRIES);
        expect(indicatorsToSelect).toEqual([Medium.gas, Medium.water]);
        expect(quantitiesToSelect).toEqual([Quantity.volume]);
      });

      it('uses the selected quantities from previous state', () => {
        const selectedListItems: uuid[] = [
          '54c58358-9631-4de3-b76c-f018fbf0fc8b',
          '22b8fd17-fd83-469e-b0ca-4ab3808beebb',
        ];
        const store = configureMockStore({
          ...initialState,
          report: {
            selectedListItems,
          },
          ui: selectedUiQuantitiesFrom([Quantity.flow])
        });

        store.dispatch(toggleSingleEntry('9ac413ed-ba1f-48d5-9793-7e259841595f'));

        expect(store.getActions()).toEqual([
          {
            type: SET_SELECTED_ENTRIES,
            payload: {
              ids: [...selectedListItems, '9ac413ed-ba1f-48d5-9793-7e259841595f'],
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
            selectedListItems: [
              '54c58358-9631-4de3-b76c-f018fbf0fc8b',
              '22b8fd17-fd83-469e-b0ca-4ab3808beebb',
            ],
          },
        });

        store.dispatch(toggleSingleEntry('9ac413ed-ba1f-48d5-9793-7e259841595f'));

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

      store.dispatch(toggleSingleEntry('54c58358-9631-4de3-b76c-f018fbf0fc8b'));

      const actions = store.getActions();
      expect(actions).toHaveLength(1);

      const {type, payload: {ids}} = actions[0];
      expect(type).toEqual(SET_SELECTED_ENTRIES);
      expect(ids).toEqual([
        '905a785e-f215-4eb8-b31c-0a00a365a124',
        'sweden,höganäs,hasselgatan 4',
        '54c58358-9631-4de3-b76c-f018fbf0fc8b',
      ]);
    });

    it('dispatches action to REMOVE id from selected entries if already selected', () => {
      const store = configureMockStore(initialState);

      store.dispatch(toggleSingleEntry('905a785e-f215-4eb8-b31c-0a00a365a124'));

      const actions = store.getActions();
      expect(actions).toHaveLength(1);

      const {type, payload: {ids}} = actions[0];
      expect(type).toEqual(SET_SELECTED_ENTRIES);
      expect(ids).toContain('sweden,höganäs,hasselgatan 4');
      expect(ids).not.toContain('905a785e-f215-4eb8-b31c-0a00a365a124');
    });

  });

  describe('addToReport', () => {

    it('adds a meter that is not already selected', () => {
      const store = configureMockStore(initialState);

      store.dispatch(addToReport('22b8fd17-fd83-469e-b0ca-4ab3808beebb'));

      const actions = store.getActions();
      expect(actions).toHaveLength(1);

      const {type, payload: {ids}} = actions[0];
      expect(type).toEqual(SET_SELECTED_ENTRIES);
      expect(ids).toContain('22b8fd17-fd83-469e-b0ca-4ab3808beebb');
    });

    it('does not fire an event if meter is already selected', () => {
      const store = configureMockStore(initialState);

      store.dispatch(addToReport('905a785e-f215-4eb8-b31c-0a00a365a124'));

      expect(store.getActions()).toHaveLength(0);
    });

    it('selects report indicators', () => {
      const store = configureMockStore({
        ...initialState,
        report: {
          selectedListItems: [],
        },
      });

      store.dispatch(addToReport('54c58358-9631-4de3-b76c-f018fbf0fc8b'));

      const actions = store.getActions();
      expect(actions).toHaveLength(1);

      const {type, payload: {indicatorsToSelect, quantitiesToSelect}} = actions[0];
      expect(type).toEqual(SET_SELECTED_ENTRIES);
      expect(indicatorsToSelect).toEqual([Medium.water]);
      expect(quantitiesToSelect).toEqual([Quantity.volume]);
    });

  });

  describe('toggleIncludingChildren', () => {

    it('selects given address and meters, if address not already selected', () => {
      const store = configureMockStore(initialState);
      store.dispatch(toggleIncludingChildren('sweden,höganäs,storgatan 5'));

      const actions = store.getActions();
      expect(actions).toHaveLength(1);

      const {type, payload: {ids}} = actions[0];
      expect(type).toEqual(SET_SELECTED_ENTRIES);
      expect(ids).toContain('sweden,höganäs,storgatan 5');
      expect(ids).toContain('22b8fd17-fd83-469e-b0ca-4ab3808beebb');
    });

    it('deselects given address and meters, if address already selected', () => {
      const store = configureMockStore(initialState);
      store.dispatch(toggleIncludingChildren('sweden,höganäs,hasselgatan 4'));

      const actions = store.getActions();
      expect(actions).toHaveLength(1);

      const {type, payload: {ids}} = actions[0];
      expect(type).toEqual(SET_SELECTED_ENTRIES);
      expect(ids).toHaveLength(0);
    });

    it('selects given clusters and meters, if cluster not already selected', () => {
      const store = configureMockStore(initialState);
      store.dispatch(toggleIncludingChildren('sweden,höganäs:s'));

      const actions = store.getActions();
      expect(actions).toHaveLength(1);

      const {type, payload: {ids}} = actions[0];
      expect(type).toEqual(SET_SELECTED_ENTRIES);
      expect(ids).toContain('sweden,höganäs:s');
      expect(ids).toContain('sweden,höganäs,storgatan 5');
      expect(ids).toContain('22b8fd17-fd83-469e-b0ca-4ab3808beebb');
    });

    it('deselects given clusters and meters, if cluster already selected', () => {
      const cluster = 'sweden,höganäs:h';

      const store = configureMockStore({
        ...initialState,
        report: {
          ...initialState.report,
          selectedListItems: [
            ...initialState.report!.selectedListItems,
            cluster,
          ],
        },
      });
      store.dispatch(toggleIncludingChildren(cluster));

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

      it('shows failure message if trying to select more than 20 meters at a time', () => {
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

        for (let i = 0; i < 30; i++) {
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
        store.dispatch(toggleIncludingChildren(addressId));

        const actions = store.getActions();
        expect(actions).toHaveLength(1);

        const {type, payload} = actions[0];
        expect(type).toEqual(SHOW_FAIL_MESSAGE);
        expect(payload).toEqual('Only 20 meters can be selected at the same time');
      });

    });

  });

  describe('selectEntryAdd', () => {

    it('adds new id to selected', () => {
      const store = configureMockStore(initialState);

      store.dispatch(selectEntryAdd('22b8fd17-fd83-469e-b0ca-4ab3808beebb'));

      expect(store.getActions()).toEqual([
        {
          type: SET_SELECTED_ENTRIES,
          payload: {
            ids: [...initialState.report.selectedListItems, '22b8fd17-fd83-469e-b0ca-4ab3808beebb'],
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

      store.dispatch(selectEntryAdd('22b8fd17-fd83-469e-b0ca-4ab3808beebb'));

      expect(store.getActions()).toEqual([
        {
          type: SET_SELECTED_ENTRIES,
          payload: {
            ids: [...initialState.report.selectedListItems, '22b8fd17-fd83-469e-b0ca-4ab3808beebb'],
            indicatorsToSelect: [Medium.gas],
            quantitiesToSelect: [Quantity.flow]
          }
        }
      ]);
    });

    it('does not dispatch when id already exist in selected', () => {
      const store = configureMockStore(initialState);

      store.dispatch(selectEntryAdd('905a785e-f215-4eb8-b31c-0a00a365a124'));

      expect(store.getActions()).toHaveLength(0);
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

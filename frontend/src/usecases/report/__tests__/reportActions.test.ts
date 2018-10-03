import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {Medium} from '../../../components/indicators/indicatorWidgetModels';
import {idGenerator} from '../../../helpers/idGenerator';
import {initTranslations} from '../../../i18n/__tests__/i18nMock';
import {RootState} from '../../../reducers/rootReducer';
import {SHOW_FAIL_MESSAGE} from '../../../state/ui/message/messageActions';
import {Action} from '../../../types/Types';
import {
  addToReport,
  SelectedEntriesPayload,
  selectEntryAdd,
  SET_SELECTED_ENTRIES,
  toggleIncludingChildren,
  toggleSingleEntry,
} from '../reportActions';
import {ReportState} from '../reportModels';

const configureMockStore = configureStore([thunk]);

type ExpectedActions = Array<Action<SelectedEntriesPayload>>;

describe('reportActions', () => {

  describe('toggleSingleEntry', () => {

    it('dispatches action to ADD id to selected entries if not already selected', () => {
      const initialState: ReportState = {selectedListItems: [2, 3]};
      const store = configureMockStore({report: {...initialState}});

      store.dispatch(toggleSingleEntry(1));

      const expected: ExpectedActions = [
        {
          type: SET_SELECTED_ENTRIES,
          payload: {
            ids: [2, 3, 1],
          },
        },
      ];

      expect(store.getActions()).toEqual(expected);

    });

    it('dispatches action to REMOVE id from selected entries if already selected', () => {
      const initialState: ReportState = {selectedListItems: [1, 2, 3]};
      const store = configureMockStore({report: {...initialState}});

      store.dispatch(toggleSingleEntry(1));

      const expected: ExpectedActions = [
        {
          type: SET_SELECTED_ENTRIES,
          payload: {
            ids: [2, 3],
          },
        },
      ];
      expect(store.getActions()).toEqual(expected);
    });

  });

  describe('addToReport', () => {

    it('adds a meter that is not already selected', () => {
      const initialState: ReportState = {selectedListItems: [1, 2, 3]};
      const store = configureMockStore({report: {...initialState}});

      store.dispatch(addToReport(4));


      const expected: ExpectedActions = [
        {
          type: SET_SELECTED_ENTRIES,
          payload: {
            ids: [1, 2, 3, 4],
          },
        },
      ];
      expect(store.getActions()).toEqual(expected);
    });

    it('does not fire an event if meter is already selected', () => {
      const initialState: ReportState = {selectedListItems: [1, 2, 3]};
      const store = configureMockStore({report: {...initialState}});

      store.dispatch(addToReport(1));

      expect(store.getActions()).toHaveLength(0);
    });

  });

  describe('toggleIncludingChildren', () => {

    const initialState: Pick<RootState, 'report' | 'selectionTree'> = {
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
              medium: [Medium.gas, Medium.water],
              name: 'höganäs',
              addresses: [
                'sweden,höganäs,hasselgatan 4',
                'sweden,höganäs,storgatan 5',
                'sweden,höganäs,väpnaregatan 10',
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
          },
        },
        result: {
          cities: [
            'sweden,höganäs',
          ],
        },
      },
    };

    it('selects given address and meters, if address not already selected', () => {
      const store = configureMockStore(initialState);
      store.dispatch(toggleIncludingChildren('sweden,höganäs,storgatan 5'));


      const expected: ExpectedActions = [{
        type: SET_SELECTED_ENTRIES,
        payload: {
          ids: [
            ...initialState.report!.selectedListItems,
            'sweden,höganäs,storgatan 5',
            '22b8fd17-fd83-469e-b0ca-4ab3808beebb',
          ],
        },
      }];
      expect(store.getActions()).toEqual(expected);
    });

    it('deselects given address and meters, if address already selected', () => {
      const store = configureMockStore(initialState);
      store.dispatch(toggleIncludingChildren('sweden,höganäs,hasselgatan 4'));

      const expected: ExpectedActions = [{
        type: SET_SELECTED_ENTRIES,
        payload: {
          ids: [],
        },
      }];
      expect(store.getActions()).toEqual(expected);
    });

    it('selects given clusters and meters, if cluster not already selected', () => {
      const store = configureMockStore(initialState);
      store.dispatch(toggleIncludingChildren('sweden,höganäs:s'));

      const expected: ExpectedActions = [{
        type: SET_SELECTED_ENTRIES,
        payload: {
          ids: [
            ...initialState.report!.selectedListItems,
            'sweden,höganäs:s',
            'sweden,höganäs,storgatan 5',
            '22b8fd17-fd83-469e-b0ca-4ab3808beebb',
          ],
        },
      }];
      expect(store.getActions()).toEqual(expected);
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

      const expected: ExpectedActions = [{
        type: SET_SELECTED_ENTRIES,
        payload: {
          ids: [],
        },
      }];
      expect(store.getActions()).toEqual(expected);
    });

    it('shows failure message if trying to select more than 20 meters at a time', () => {
      const translations = {
        code: 'en',
        translation: {
          'only {{limit}} meters can be selected at the same time':
            'only {{limit}} meters can be selected at the same time',
        },
      };
      initTranslations(translations);
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

      const expected = [{
        type: SHOW_FAIL_MESSAGE,
        payload: 'Only 20 meters can be selected at the same time',
      }];
      expect(store.getActions()).toEqual(expected);
    });

  });

  describe('selectEntryAdd', () => {

    it('adds id to selected if not there', () => {
      const initialState: ReportState = {selectedListItems: [1, 2]};
      const store = configureMockStore({report: {...initialState}});

      store.dispatch(selectEntryAdd(3));

      const expected: ExpectedActions = [
        {
          type: SET_SELECTED_ENTRIES,
          payload: {
            ids: [1, 2, 3],
          },
        },
      ];
      expect(store.getActions()).toEqual(expected);
    });

    it('does not dispatch when id already exist in selected', () => {
      const initialState: ReportState = {selectedListItems: [1, 2, 3]};
      const store = configureMockStore({report: {...initialState}});

      store.dispatch(selectEntryAdd(3));

      const expected: ExpectedActions = [];
      expect(store.getActions()).toEqual(expected);
    });

  });

});

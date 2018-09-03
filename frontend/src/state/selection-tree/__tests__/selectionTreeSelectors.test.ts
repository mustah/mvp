import {SelectionTree, SelectionTreeState} from '../selectionTreeModels';
import {getSelectionTree} from '../selectionTreeSelectors';

describe('selectionTreeSelectors', () => {

  describe('getSelectionTree', () => {

    const selectionTreeState: SelectionTreeState = {
      isFetching: false,
      isSuccessfullyFetched: true,
      entities: {
        cities: {
          'sweden,kungsbacka': {
            id: 'sweden,kungsbacka',
            name: 'kungsbacka',
            addresses: ['sweden,kungsbacka,kabelgatan 2', 'sweden,kungsbacka,kabelgatan 3'],
          },
          'sweden,gothenburg': {
            id: 'sweden,gothenburg',
            name: 'gothenburg',
            addresses: [
              'sweden,gothenburg,kungsgatan 2',
              'sweden,gothenburg,kungsgatan 1',
              'sweden,gothenburg,drottninggatan 1',
            ],
          },
        },
        addresses: {
          'sweden,kungsbacka,kabelgatan 2': {id: 'sweden,kungsbacka,kabelgatan 2', name: 'kabelgatan 2', meters: [1]},
          'sweden,kungsbacka,kabelgatan 3': {id: 'sweden,kungsbacka,kabelgatan 3', name: 'kabelgatan 3', meters: [2]},
          'sweden,gothenburg,kungsgatan 2': {id: 'sweden,gothenburg,kungsgatan 2', name: 'kungsgatan 2', meters: [3]},
          'sweden,gothenburg,kungsgatan 1': {id: 'sweden,gothenburg,kungsgatan 1', name: 'kungsgatan 1', meters: [4]},
          'sweden,gothenburg,drottninggatan 1': {
            id: 'sweden,gothenburg,drottninggatan 1',
            name: 'drottninggatan 1',
            meters: [5],
          },
        },
        meters: {
          1: {id: 1, name: 'extId1'},
          2: {id: 2, name: 'extId2'},
          3: {id: 3, name: 'extId3'},
          4: {id: 4, name: 'extId4'},
          5: {id: 5, name: 'extId5'},
        },
      },
      result: {
        cities: ['sweden,kungsbacka', 'sweden,gothenburg'],
      },
    };

    it('regroups selectionTreeState into selectionTree', () => {
      const expected: SelectionTree = {
        entities: {
          ...selectionTreeState.entities,
          cities: {
            'sweden,kungsbacka': {
              id: 'sweden,kungsbacka',
              name: 'kungsbacka',
              clusters: ['sweden,kungsbacka:k'],
            },
            'sweden,gothenburg': {
              id: 'sweden,gothenburg',
              name: 'gothenburg',
              clusters: [
                'sweden,gothenburg:k',
                'sweden,gothenburg:d',
              ],
            },
          },
          clusters: {
            'sweden,kungsbacka:k': {
              id: 'sweden,kungsbacka:k',
              name: 'K...(2)',
              addresses: ['sweden,kungsbacka,kabelgatan 2', 'sweden,kungsbacka,kabelgatan 3'],
            },
            'sweden,gothenburg:k': {
              id: 'sweden,gothenburg:k',
              name: 'K...(2)',
              addresses: ['sweden,gothenburg,kungsgatan 2', 'sweden,gothenburg,kungsgatan 1'],
            },
            'sweden,gothenburg:d': {
              id: 'sweden,gothenburg:d',
              name: 'D...(1)',
              addresses: ['sweden,gothenburg,drottninggatan 1'],
            },
          },
        },
        result: {...selectionTreeState.result},
      };

      expect(getSelectionTree(selectionTreeState)).toEqual(expected);

    });

    it('handles an empty state', () => {
      const selectionTreeState: SelectionTreeState = {
        isFetching: false,
        isSuccessfullyFetched: true,
        entities: {
          cities: {},
          addresses: {},
          meters: {},
        },
        result: {cities: []},
      };
      expect(getSelectionTree(selectionTreeState)).toEqual({
        entities: {
          cities: {},
          clusters: {},
          addresses: {},
          meters: {},
        },
        result: {cities: []},
      });
    });

    it('memoizes result', () => {
      const expected = getSelectionTree(selectionTreeState);
      expect(getSelectionTree(selectionTreeState)).toBe(expected);
    });

    describe('handles wildcard query', () => {

      it('limits city by searching for city', () => {
        const withQuery = getSelectionTree({...selectionTreeState, query: 'kungsbacka'});
        const withoutQuery = getSelectionTree({...selectionTreeState});

        expect(withoutQuery.entities.cities).toHaveProperty('sweden,gothenburg');
        expect(withQuery.entities.cities['sweden,gothenburg']).toBeUndefined();
      });

      it('limits city by searching for address name', () => {
        const withQuery = getSelectionTree({...selectionTreeState, query: 'kabelgatan 3'});
        const withoutQuery = getSelectionTree({...selectionTreeState});

        expect(withoutQuery.entities.cities).toHaveProperty('sweden,gothenburg');
        expect(withQuery.entities.cities['sweden,gothenburg']).toBeUndefined();

        expect(withoutQuery.entities.clusters['sweden,kungsbacka:k'].name).toEqual('K...(2)');
        expect(withQuery.entities.clusters['sweden,kungsbacka:k'].name).toEqual('K...(1)');

      });

    });

  });

});

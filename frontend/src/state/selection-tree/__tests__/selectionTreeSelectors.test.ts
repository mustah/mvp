import {uuid} from '../../../types/Types';
import {ObjectsById} from '../../domain-models/domainModels';
import {Medium} from '../../ui/graph/measurement/measurementModels';
import {SelectedTreeEntities, SelectionTree, SelectionTreeMeter, SelectionTreeState} from '../selectionTreeModels';
import {getMedia, getMeterIds, getMeterIdsWithLimit, getSelectionTree} from '../selectionTreeSelectors';

describe('selectionTreeSelectors', () => {

  const selectionTreeState: SelectionTreeState = {
    isFetching: false,
    isSuccessfullyFetched: true,
    entities: {
      cities: {
        'sweden,kungsbacka': {
          id: 'sweden,kungsbacka',
          city: 'sweden,kungsbacka',
          medium: [Medium.water],
          name: 'kungsbacka',
          addresses: ['sweden,kungsbacka,kabelgatan 2', 'sweden,kungsbacka,kabelgatan 3'],
        },
        'sweden,gothenburg': {
          id: 'sweden,gothenburg',
          city: 'sweden,gothenburg',
          medium: [Medium.water, Medium.gas],
          name: 'gothenburg',
          addresses: [
            'sweden,gothenburg,kungsgatan 2',
            'sweden,gothenburg,kungsgatan 1',
            'sweden,gothenburg,drottninggatan 1',
          ],
        },
        'denmark,copenhagen': {
          id: 'denmark,copenhagen',
          city: 'denmark,copenhagen',
          medium: [],
          name: 'copenhagen',
          addresses: ['denmark,copenhagen,kabelgatan 2'],
        },
      },
      addresses: {
        'sweden,kungsbacka,kabelgatan 2': {
          address: 'kabelgatan 2',
          city: 'sweden,kungsbacka',
          id: 'sweden,kungsbacka,kabelgatan 2',
          name: 'kabelgatan 2',
          meters: [1],
        },
        'sweden,kungsbacka,kabelgatan 3': {
          address: 'kabelgatan 3',
          city: 'sweden,kungsbacka',
          id: 'sweden,kungsbacka,kabelgatan 3',
          name: 'kabelgatan 3',
          meters: [2],
        },
        'sweden,gothenburg,kungsgatan 2': {
          address: 'kungsgatan 2',
          city: 'sweden,gothenburg',
          id: 'sweden,gothenburg,kungsgatan 2',
          name: 'kungsgatan 2',
          meters: [3],
        },
        'sweden,gothenburg,kungsgatan 1': {
          address: 'kungsgatan 1',
          city: 'sweden,gothenburg',
          id: 'sweden,gothenburg,kungsgatan 1',
          name: 'kungsgatan 1',
          meters: [4],
        },
        'sweden,gothenburg,drottninggatan 1': {
          address: 'drottninggatan 1',
          city: 'sweden,gothenburg',
          id: 'sweden,gothenburg,drottninggatan 1',
          name: 'drottninggatan 1',
          meters: [5],
        },
      },
      meters: {
        1: {
          address: 'kabelgatan 2',
          city: 'sweden,kungsbacka',
          id: 1,
          name: 'extId1',
          medium: Medium.water,
        },
        2: {
          address: 'kabelgatan 3',
          city: 'sweden,kungsbacka',
          id: 2,
          name: 'extId2',
          medium: Medium.water,
        },
        3: {
          address: 'kungsgatan 2',
          city: 'sweden,gothenburg',
          id: 3,
          name: 'extId3',
          medium: Medium.water,
        },
        4: {
          address: 'kungsgatan 1',
          city: 'sweden,gothenburg',
          id: 4,
          name: 'extId4',
          medium: Medium.gas,
        },
        5: {
          address: 'drottninggatan 1',
          city: 'sweden,gothenburg',
          id: 5,
          name: 'extId5',
          medium: Medium.gas,
        },
        6: {
          id: 6,
          address: 'kabelgatan 2',
          city: 'denmark,copenhagen',
          medium: Medium.unknown,
          name: 'ext6',
        },
      },
    },
    result: {
      cities: ['sweden,kungsbacka', 'sweden,gothenburg'],
    },
  };

  describe('getSelectionTree', () => {

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

  describe('getMedia', () => {

    it('gets media from cities', () => {
      const selectedListItems: uuid[] = ['sweden,kungsbacka', 'sweden,gothenburg'];
      const expected: Set<Medium> = new Set([Medium.gas, Medium.water]);

      const state: SelectedTreeEntities = {
        selectedListItems,
        entities: {...selectionTreeState.entities},
      };

      expect(getMedia(state)).toEqual(expected);
    });

    it('gets media from meters and cities', () => {
      const selectedListItems: uuid[] = ['sweden,kungsbacka', '4'];
      const expected: Set<Medium> = new Set([Medium.gas, Medium.water]);

      const state: SelectedTreeEntities = {
        selectedListItems,
        entities: {...selectionTreeState.entities},
      };

      expect(getMedia(state)).toEqual(expected);
    });

  });

  describe('getMeterIds', () => {

    it('gets all the meters', () => {
      const expected: uuid[] = ['1', '2', '3', '4', '5', '6'];
      const actual: uuid[] = getMeterIds(selectionTreeState);

      expect(actual).toEqual(expected);
    });

  });

  describe('getMeterIdsWithLimit', () => {

    it('handles no selection tree meters', () => {
      expect(getMeterIdsWithLimit()).toEqual([]);
    });

    it('handles empty selection tree meters', () => {
      const meters: ObjectsById<SelectionTreeMeter> = {};

      expect(getMeterIdsWithLimit(meters)).toEqual([]);
    });

    it('should only have one meter id', () => {
      const meters: ObjectsById<SelectionTreeMeter> = {
        1: {id: 1, name: 'a', address: 'b', city: 'c', medium: Medium.gas},
      };
      expect(getMeterIdsWithLimit(meters)).toEqual(['1']);
    });

    it('should only have more than one meter id', () => {
      const meters: ObjectsById<SelectionTreeMeter> = {
        1: {id: 1, name: 'a', address: 'b', city: 'c', medium: Medium.gas},
        2: {id: 2, name: 'a', address: 'b', city: 'c', medium: Medium.gas},
      };
      expect(getMeterIdsWithLimit(meters)).toEqual(['1', '2']);
    });

  });

});

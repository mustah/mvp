import {makeMeter} from '../../../../__tests__/testDataFactory';
import {HasId, uuid} from '../../../../types/Types';
import {NormalizedState, ObjectsById} from '../../../domain-models/domainModels';
import {Meter, SelectionTreeData} from '../meterModels';
import {getMeterDataSummary, getSelectionTree} from '../meterSelectors';

type PartialDomainModel = ObjectsById<Partial<Meter> & HasId>;
describe('meterSelectors', () => {

  describe('summary', () => {
    it('can group a list of meters with a tally each (like, 5 in NY, 3 in LA)', () => {
      const meterIds: uuid[] = [1, 2, 3];
      const meters: PartialDomainModel = {
        1: {
          id: 1,
          flagged: false,
          city: {id: 'sto', name: 'stockholm'},
          manufacturer: 'ELV',
          medium: 'water',
          status: {id: 0, name: 'ok'},
          alarm: 'none',
        },
        2: {
          id: 2,
          flagged: false,
          city: {id: 'sto', name: 'stockholm'},
          manufacturer: 'ELV',
          medium: 'air',
          status: {id: 0, name: 'ok'},
          alarm: 'none',
        },
        3: {
          id: 3,
          flagged: true,
          city: {id: 'got', name: 'göteborg'},
          manufacturer: 'ELV',
          medium: 'air',
          status: {id: 0, name: 'ok'},
          alarm: 'none',
        },
      };

      const metersState: Partial<NormalizedState<Meter>> = {
        entities: meters as ObjectsById<Meter>,
        result: meterIds,
      };

      const reduced = getMeterDataSummary(metersState as NormalizedState<Meter>);

      expect(reduced.get()).toEqual({
        flagged:
          {
            flagged: {name: 'flagged', value: 1, filterParam: true},
            unFlagged: {name: 'unFlagged', value: 2, filterParam: false},
          },
        city:
          {
            sto: {name: 'stockholm', value: 2, filterParam: 'sto'},
            got: {name: 'göteborg', value: 1, filterParam: 'got'},
          },
        manufacturer: {
          ELV: {name: 'ELV', value: 3, filterParam: 'ELV'},
        },
        medium:
          {
            water: {name: 'water', value: 1, filterParam: 'water'},
            air: {name: 'air', value: 2, filterParam: 'air'},
          },
        status: {
          0: {name: 'ok', value: 3, filterParam: 0},
        },
        alarm: {
          none: {name: 'none', value: 3, filterParam: 'none'},
        },
      });
    });
  });

  describe('selection tree', () => {

    it('handles mismatches between result list and actual entities', () => {
      const metersState: NormalizedState<Meter> = {
        isFetching: false,
        total: 4,
        result: [1, 2, 3, 4],
        entities: {},
      };
      const actualTree: SelectionTreeData = getSelectionTree(metersState);

      const expected: SelectionTreeData = {
        result: {
          addressClusters: [],
          addresses: [],
          cities: [],
          meters: [],
        },
        entities: {},
      };
      expect(actualTree).toEqual(expected);
    });

    it('can make a tree of meters; categorized by cities, addresses and such', () => {
      const metersState: NormalizedState<Meter> = {
        isFetching: false,
        total: 4,
        result: [1, 2, 3],
        entities: {
          1: makeMeter(1, 1, 'Helsingborg', 1, 'Storgatan 5'),
          2: makeMeter(2, 1, 'Helsingborg', 2, 'Storgatan 6'),
          3: makeMeter(3, 2, 'Luleå', 3, 'Ringvägen 7'),
        },
      };
      const actualTree: SelectionTreeData = getSelectionTree(metersState);

      const expected: SelectionTreeData = {
        entities: {
          addressClusters: {
            'Helsingborg:S': {
              childNodes: {
                ids: [1, 2],
                type: 'addresses',
              },
              id: 'Helsingborg:S',
              name: 'S...(2)',
              parent: {id: 1, type: 'cities'},
              selectable: false,
            },
            'Luleå:R': {
              childNodes: {ids: [3], type: 'addresses'},
              id: 'Luleå:R',
              name: 'R...(1)',
              parent: {id: 2, type: 'cities'},
              selectable: false,
            },
          },
          addresses: {
            1: {
              childNodes: {ids: [1], type: 'meters'},
              id: 1,
              name: 'Storgatan 5',
              parent: {id: 'Helsingborg:S', type: 'addressClusters'},
              selectable: true,
            },
            2: {
              childNodes: {ids: [2], type: 'meters'},
              id: 2,
              name: 'Storgatan 6',
              parent: {id: 'Helsingborg:S', type: 'addressClusters'},
              selectable: true,
            },
            3: {
              childNodes: {ids: [3], type: 'meters'},
              id: 3,
              name: 'Ringvägen 7',
              parent: {id: 'Luleå:R', type: 'addressClusters'},
              selectable: true,
            },
          },
          cities: {
            1: {
              childNodes: {ids: ['Helsingborg:S'], type: 'addressClusters'},
              id: 1,
              name: 'Helsingborg',
              parent: {id: '', type: ''},
              selectable: true,
            },
            2: {
              childNodes: {ids: ['Luleå:R'], type: 'addressClusters'},
              id: 2,
              name: 'Luleå',
              parent: {id: '', type: ''},
              selectable: true,
            },
          },
          meters: {
            1: {
              childNodes: {ids: [], type: ''},
              id: 1,
              name: '1',
              parent: {id: 1, type: 'addresses'},
              selectable: true,
            },
            2: {
              childNodes: {ids: [], type: ''},
              id: 2,
              name: '1',
              parent: {id: 2, type: 'addresses'},
              selectable: true,
            },
            3: {
              childNodes: {ids: [], type: ''},
              id: 3,
              name: '1',
              parent: {id: 3, type: 'addresses'},
              selectable: true,
            },
          },
        },
        result: {
          addressClusters: ['Helsingborg:S', 'Luleå:R'],
          addresses: [1, 2, 3],
          cities: [1, 2],
          meters: [1, 2, 3],
        },
      };
      expect(actualTree).toEqual(expected);
    });

  });
});

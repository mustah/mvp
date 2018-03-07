import {makeMeter} from '../../../../__tests__/testDataFactory';
import {HasId, uuid} from '../../../../types/Types';
import {Meter} from '../../../domain-models-paginated/meter/meterModels';
import {NormalizedState, ObjectsById} from '../../domainModels';
import {Location} from '../../location/locationModels';
import {MeterDataSummary, SelectionTreeData} from '../allMetersModels';
import {getMeterDataSummary, getSelectionTree} from '../allMetersSelectors';

type PartialDomainModel = ObjectsById<Partial<Meter> & HasId>;
describe('allMetersSelectors', () => {

  describe('summary', () => {

    it('can group a list of meters with a tally each (like, 5 in NY, 3 in LA)', () => {
      const meterIds: uuid[] = [1, 2, 3];
      const stockholm: Partial<Location> = {
        city: {id: 'sto', name: 'stockholm'},
      };
      const gothenburg: Partial<Location> = {
        city: {id: 'got', name: 'göteborg'},
      };
      const meters: PartialDomainModel = {
        1: {
          id: 1,
          flagged: false,
          location: stockholm as Location,
          manufacturer: 'ELV',
          medium: 'water',
          status: {id: 0, name: 'ok'},
          alarm: 'none',
        },
        2: {
          id: 2,
          flagged: false,
          location: stockholm as Location,
          manufacturer: 'ELV',
          medium: 'air',
          status: {id: 0, name: 'ok'},
          alarm: 'none',
        },
        3: {
          id: 3,
          flagged: true,
          location: gothenburg as Location,
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

      const expected: MeterDataSummary = {
        flagged:
          {
            unFlagged: {name: 'unFlagged', value: 2, filterParam: false},
            flagged: {name: 'flagged', value: 1, filterParam: true},
          },
        location:
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
      };
      expect(reduced.get()).toEqual(expected);
    });
  });

  describe('selection tree', () => {

    it('handles mismatches between result list and actual entities', () => {
      const metersState: NormalizedState<Meter> = {
        isFetching: false,
        isSuccessfullyFetched: true,
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
        isSuccessfullyFetched: true,
        total: 4,
        result: [1, 2, 3],
        entities: {
          1: makeMeter(1, 'city1', 'city1', 'address1', 'address1'),
          2: makeMeter(2, 'city1', 'city1', 'address2', 'address2'),
          3: makeMeter(3, 'city2', 'city2', 'address3', 'address3'),
        },
      };
      const actualTree: SelectionTreeData = getSelectionTree(metersState);

      const expected: SelectionTreeData = {
        entities: {
          addressClusters: {
            'city1:A': {
              childNodes: {
                ids: ['address1', 'address2'],
                type: 'addresses',
              },
              id: 'city1:A',
              name: 'A...(2)',
              parent: {id: 'city1', type: 'cities'},
              selectable: false,
            },
            'city2:A': {
              childNodes: {ids: ['address3'], type: 'addresses'},
              id: 'city2:A',
              name: 'A...(1)',
              parent: {id: 'city2', type: 'cities'},
              selectable: false,
            },
          },
          addresses: {
            address1: {
              childNodes: {ids: [1], type: 'meters'},
              id: 'address1',
              name: 'address1',
              parent: {id: 'city1:A', type: 'addressClusters'},
              selectable: true,
            },
            address2: {
              childNodes: {ids: [2], type: 'meters'},
              id: 'address2',
              name: 'address2',
              parent: {id: 'city1:A', type: 'addressClusters'},
              selectable: true,
            },
            address3: {
              childNodes: {ids: [3], type: 'meters'},
              id: 'address3',
              name: 'address3',
              parent: {id: 'city2:A', type: 'addressClusters'},
              selectable: true,
            },
          },
          cities: {
            city1: {
              childNodes: {ids: ['city1:A'], type: 'addressClusters'},
              id: 'city1',
              name: 'city1',
              parent: {id: '', type: ''},
              selectable: true,
            },
            city2: {
              childNodes: {ids: ['city2:A'], type: 'addressClusters'},
              id: 'city2',
              name: 'city2',
              parent: {id: '', type: ''},
              selectable: true,
            },
          },
          meters: {
            1: {
              childNodes: {ids: [], type: ''},
              id: 1,
              name: '1',
              parent: {id: 'address1', type: 'addresses'},
              selectable: true,
            },
            2: {
              childNodes: {ids: [], type: ''},
              id: 2,
              name: '1',
              parent: {id: 'address2', type: 'addresses'},
              selectable: true,
            },
            3: {
              childNodes: {ids: [], type: ''},
              id: 3,
              name: '1',
              parent: {id: 'address3', type: 'addresses'},
              selectable: true,
            },
          },
        },
        result: {
          addressClusters: ['city1:A', 'city2:A'],
          addresses: ['address1', 'address2', 'address3'],
          cities: ['city1', 'city2'],
          meters: [1, 2, 3],
        },
      };
      expect(actualTree).toEqual(expected);
    });

  });
});

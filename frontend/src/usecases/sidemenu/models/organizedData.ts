import {normalize} from 'normalizr';
import {selectionSchema} from '../../../state/search/selection/selectionSchemas';
import {uuid} from '../../../types/Types';

export interface SelectionTree {
  [key: string]: Array<{
    id: uuid;
    name: string;
    parent: {type: string; id: uuid};
    childNodes: {type: string; ids: uuid[]};
  }>;
}

const organizedData: SelectionTree = {
  cities: [
    {
      id: 'got',
      name: 'Göteborg',
      parent: {type: '', id: ''},
      childNodes: {type: 'addresses', ids: [1, 2]},
    },
    {
      id: 'sto',
      name: 'Stockholm',
      parent: {type: '', id: ''},
      childNodes: {type: 'addresses', ids: [3]},
    },
    {
      id: 'mmx',
      name: 'Malmö',
      parent: {type: '', id: ''},
      childNodes: {type: 'addresses', ids: [4]},
    },
    {
      id: 'kub',
      name: 'Kungsbacka',
      parent: {type: '', id: ''},
      childNodes: {type: 'addresses', ids: [5]},
    },
  ],
  addresses: [
    {
      id: 1,
      name: 'Stampgatan 46',
      parent: {type: 'cities', id: 'got'},
      childNodes: {type: 'meteringPoints', ids: ['m1', 'm2', 'm3']},
    },
    {
      id: 2,
      name: 'Stampgatan 33',
      parent: {type: 'cities', id: 'got'},
      childNodes: {type: 'meteringPoints', ids: ['m4']},
    },
    {
      id: 3,
      name: 'Kungsgatan 44',
      parent: {type: 'cities', id: 'sto'},
      childNodes: {type: 'meteringPoints', ids: ['m5']},
    },
    {
      id: 4,
      name: 'Drottninggatan 1',
      parent: {type: 'cities', id: 'mmx'},
      childNodes: {type: 'meteringPoints', ids: ['m6', 'm7', 'm8']},
    },
    {
      id: 5,
      name: 'Åvägen 9',
      parent: {type: 'cities', id: 'kub'},
      childNodes: {type: 'meteringPoints', ids: ['m9', 'm10']},
    },
  ],
  meteringPoints: [
    {
      id: 'm1',
      name: 'UNICOcoder',
      parent: {type: 'addresses', id: 1},
      childNodes: {type: '', ids: []},
    },
    {
      id: 'm2',
      name: '3100',
      parent: {type: 'addresses', id: 1},
      childNodes: {type: '', ids: []},
    },
    {
      id: 'm3',
      name: 'xxx2233',
      parent: {type: 'addresses', id: 1},
      childNodes: {type: '', ids: []},
    },
    {
      id: 'm4',
      name: '3100',
      parent: {type: 'addresses', id: 2},
      childNodes: {type: '', ids: []},
    },
    {
      id: 'm5',
      name: 'Test kit',
      parent: {type: 'addresses', id: 3},
      childNodes: {type: '', ids: []},
    },
    {
      id: 'm6',
      name: 'UNICOcoder',
      parent: {type: 'addresses', id: 4},
      childNodes: {type: '', ids: []},
    },
    {
      id: 'm7',
      name: '3100',
      parent: {type: 'addresses', id: 4},
      childNodes: {type: '', ids: []},
    },
    {
      id: 'm8',
      name: 'xxx2233',
      parent: {type: 'addresses', id: 4},
      childNodes: {type: '', ids: []},
    },
    {
      id: 'm9',
      name: '3100',
      parent: {type: 'addresses', id: 5},
      childNodes: {type: '', ids: []},
    },
    {
      id: 'm10',
      name: 'Test kit',
      parent: {type: 'addresses', id: 5},
      childNodes: {type: '', ids: []},
    },
  ],
};

export const selectionTreeData = normalize(organizedData, selectionSchema);

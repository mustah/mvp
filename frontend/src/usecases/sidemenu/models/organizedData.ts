import {normalize} from 'normalizr';
import {selectionSchema} from '../../../state/search/selection/selectionSchemas';
import {uuid} from '../../../types/Types';

export interface SelectionTree {
  [key: string]: {
    id: uuid;
    name: string;
    parent: uuid;
    childNodes: uuid[];
  }[];
}

const organizedData: SelectionTree = {
  cities: [
    {
      id: 'got',
      name: 'Göteborg',
      parent: '',
      childNodes: [1, 2],
    },
    {
      id: 'sto',
      name: 'Stockholm',
      parent: '',
      childNodes: [3],
    },
    {
      id: 'mmx',
      name: 'Malmö',
      parent: '',
      childNodes: [4],
    },
    {
      id: 'kub',
      name: 'Kungsbacka',
      parent: '',
      childNodes: [5],
    },
  ],
  addresses: [
    {
      id: 1,
      name: 'Stampgatan 46',
      parent: 'got',
      childNodes: ['m1', 'm2', 'm3'],
    },
    {
      id: 2,
      name: 'Stampgatan 33',
      parent: 'got',
      childNodes: ['m4'],
    },
    {
      id: 3,
      name: 'Kungsgatan 44',
      parent: 'sto',
      childNodes: ['m5'],
    },
    {
      id: 4,
      name: 'Drottninggatan 1',
      parent: 'mmx',
      childNodes: ['m6', 'm7', 'm8'],
    },
    {
      id: 5,
      name: 'Åvägen 9',
      parent: 'kub',
      childNodes: ['m9', 'm10'],
    },
  ],
  meteringPoints: [
    {
      id: 'm1',
      name: 'UNICOcoder',
      parent: 1,
      childNodes: [],
    },
    {
      id: 'm2',
      name: '3100',
      parent: 1,
      childNodes: [],
    },
    {
      id: 'm3',
      name: 'xxx2233',
      parent: 1,
      childNodes: [],
    },
    {
      id: 'm4',
      name: '3100',
      parent: 2,
      childNodes: [],
    },
    {
      id: 'm5',
      name: 'Test kit',
      parent: 3,
      childNodes: [],
    },
    {
      id: 'm6',
      name: 'UNICOcoder',
      parent: 4,
      childNodes: [],
    },
    {
      id: 'm7',
      name: '3100',
      parent: 4,
      childNodes: [],
    },
    {
      id: 'm8',
      name: 'xxx2233',
      parent: 4,
      childNodes: [],
    },
    {
      id: 'm9',
      name: '3100',
      parent: 5,
      childNodes: [],
    },
    {
      id: 'm10',
      name: 'Test kit',
      parent: 5,
      childNodes: [],
    },
  ],
};

export const selectionTreeData = normalize(organizedData, selectionSchema);

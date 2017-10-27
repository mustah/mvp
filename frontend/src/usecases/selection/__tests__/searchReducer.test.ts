import {normalize} from 'normalizr';
import {SelectionParameter} from '../../../state/search/selection/selectionModels';
import {setSelection} from '../../../state/search/selection/selectionActions';
import {initialState, selection} from '../../../state/search/selection/selectionReducer';
import {selectionSchema} from '../../../state/search/selection/selectionSchemas';

describe('searchReducer', () => {

  const data = {
    cities: [
      {
        id: 'got',
        name: 'Göteborg',
      },
      {
        id: 'sto',
        name: 'Stockholm',
      },
      {
        id: 'mmx',
        name: 'Malmö',
      },
      {
        id: 'kub',
        name: 'Kungsbacka',
      },
    ],
    addresses: [
      {
        id: 1,
        name: 'Stampgatan 46',
      },
      {
        id: 2,
        name: 'Stampgatan 33',
      },
      {
        id: 3,
        name: 'Kungsgatan 44',
      },
      {
        id: 4,
        name: 'Drottninggatan 1',
      },
      {
        id: 5,
        name: 'Åvägen 9',
      },
    ],
    statuses: [
      {
        id: 'ok',
        name: 'Ok',
      },
      {
        id: 'warning',
        name: 'Varning',
      },
      {
        id: 'info',
        name: 'Info',
      },
      {
        id: 'critical',
        name: 'Kritisk',
      },
    ],
    meteringPoints: [
      {
        id: 'm1',
        name: 'UNICOcoder',
      },
      {
        id: 'm2',
        name: '3100',
      },
      {
        id: 'm3',
        name: 'xxx2233',
      },
      {
        id: 'm4',
        name: '3100',
      },
      {
        id: 'm5',
        name: 'Test kit',
      },
    ],
  };

  it('adds to selected list', () => {
    const state = {
      ...initialState,
    };
    const searchParameters: SelectionParameter = {
      name: 'Stockholm',
      id: 'sto',
      attribute: 'cities',
    };
    expect(selection(state!, setSelection(searchParameters))).toEqual({
      ...initialState,
      selected: {
        ...state.selected,
        cities: ['sto'],
      },
    });
  });

  it('normalizr', () => {
    const normalizedData = normalize(data, selectionSchema);

    expect(normalizedData).toEqual({
      entities: {
        addresses: {
          1: {
            id: 1,
            name: 'Stampgatan 46',
          },
          2: {
            id: 2,
            name: 'Stampgatan 33',
          },
          3: {
            id: 3,
            name: 'Kungsgatan 44',
          },
          4: {
            id: 4,
            name: 'Drottninggatan 1',
          },
          5: {
            id: 5,
            name: 'Åvägen 9',
          },
        },
        cities: {
          got: {
            id: 'got',
            name: 'Göteborg',
          },
          kub: {
            id: 'kub',
            name: 'Kungsbacka',
          },
          mmx: {
            id: 'mmx',
            name: 'Malmö',
          },
          sto: {
            id: 'sto',
            name: 'Stockholm',
          },
        },
        meteringPoints: {
          m1: {
            id: 'm1',
            name: 'UNICOcoder',
          },
          m2: {
            id: 'm2',
            name: '3100',
          },
          m3: {
            id: 'm3',
            name: 'xxx2233',
          },
          m4: {
            id: 'm4',
            name: '3100',
          },
          m5: {
            id: 'm5',
            name: 'Test kit',
          },
        },
        statuses: {
          critical: {
            id: 'critical',
            name: 'Kritisk',
          },
          info: {
            id: 'info',
            name: 'Info',
          },
          ok: {
            id: 'ok',
            name: 'Ok',
          },
          warning: {
            id: 'warning',
            name: 'Varning',
          },
        },
      },
      result: {
        addresses: [
          1,
          2,
          3,
          4,
          5,
        ],
        cities: [
          'got',
          'sto',
          'mmx',
          'kub',
        ],
        meteringPoints: [
          'm1',
          'm2',
          'm3',
          'm4',
          'm5',
        ],
        statuses: [
          'ok',
          'warning',
          'info',
          'critical',
        ],
      },
    });
  });
});

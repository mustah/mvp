import {normalize} from 'normalizr';
import {setSelection} from '../../../state/search/selection/selectionActions';
import {entityNames, SelectionParameter} from '../../../state/search/selection/selectionModels';
import {initialState, selection} from '../../../state/search/selection/selectionReducer';
import {selectionSchema} from '../../../state/search/selection/selectionSchemas';
import {IdNamed} from '../../../types/Types';

const dbJsonData = require('./../../../../mockdata');
const mockData = dbJsonData();

describe('searchReducer', () => {

  it('adds to selected list', () => {
    const state = {...initialState};
    const stockholm: IdNamed = {...mockData.selections.cities[0]};
    const searchParameters: SelectionParameter = {...stockholm, parameter: entityNames.cities};

    expect(selection(state, setSelection(searchParameters))).toEqual({
      ...initialState,
      selected: {
        ...state.selected,
        cities: [stockholm.id],
      },
    });
  });

  it('normalized selection data', () => {
    const normalizedData = normalize(mockData.selections, selectionSchema);

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

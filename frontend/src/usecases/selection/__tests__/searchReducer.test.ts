import {normalize} from 'normalizr';
import {testData} from '../../../__tests__/TestDataFactory';
import {setSelection} from '../../../state/search/selection/selectionActions';
import {parameterNames, SelectionParameter} from '../../../state/search/selection/selectionModels';
import {initialState, selection} from '../../../state/search/selection/selectionReducer';
import {selectionSchema} from '../../../state/search/selection/selectionSchemas';
import {IdNamed} from '../../../types/Types';

describe('searchReducer', () => {

  it('adds to selected list', () => {
    const state = {...initialState};
    const stockholm: IdNamed = {...testData.geoData.cities[0]};
    const searchParameters: SelectionParameter = {...stockholm, parameter: parameterNames.cities};

    expect(selection(state, setSelection(searchParameters))).toEqual({
      ...initialState,
      selected: {
        ...state.selected,
        cities: [stockholm.id],
      },
    });
  });

  it('normalized selection data', () => {
    const normalizedData = normalize(testData.geoData, selectionSchema);

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
      },
    });
  });
});

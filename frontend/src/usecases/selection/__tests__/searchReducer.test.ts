import {normalize} from 'normalizr';
import {testData} from '../../../__tests__/TestDataFactory';
import {setSelection} from '../../../state/search/selection/selectionActions';
import {parameterNames, SelectionParameter} from '../../../state/search/selection/selectionModels';
import {initialState, selection} from '../../../state/search/selection/selectionReducer';
import {IdNamed} from '../../../types/Types';
import {selectionsSchema} from '../../../state/domain-models/domainModelsSchemas';

describe('searchReducer', () => {

  it('adds to selected list', () => {
    const state = {...initialState};
    const stockholm: IdNamed = {...testData.selections.cities[0]};
    const searchParameters: SelectionParameter = {...stockholm, parameter: parameterNames.cities};

    expect(selection(state, setSelection(searchParameters))).toEqual({
      ...initialState,
      isChanged: true,
      selected: {
        ...state.selected,
        cities: [stockholm.id],
      },
    });
  });

  it('normalized selection data', () => {
    const normalizedData = normalize(testData.selections, selectionsSchema);

    expect(normalizedData).toEqual({
      entities: {
        addresses: {
          1: {
            id: 1,
            name: 'Stampgatan 46',
            cityId: 'got',
          },
          2: {
            id: 2,
            name: 'Stampgatan 33',
            cityId: 'got',
          },
          3: {
            id: 3,
            name: 'Kungsgatan 44',
            cityId: 'sto',
          },
          4: {
            id: 4,
            name: 'Drottninggatan 1',
            cityId: 'mmx',
          },
          5: {
            id: 5,
            name: 'Åvägen 9',
            cityId: 'kub',
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
        alarms: [],
        cities: [
          'got',
          'sto',
          'mmx',
          'kub',
        ],
        manufacturers: [],
        productModels: [],
      },
    });
  });
});

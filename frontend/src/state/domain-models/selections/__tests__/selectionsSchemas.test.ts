import {testData} from '../../../../__tests__/testDataFactory';
import {selectionsDataFormatter} from '../selectionsSchemas';

describe('normalize state', () => {

  const normalizedData = selectionsDataFormatter(testData.selections);

  it('normalized selection data', () => {
    expect(normalizedData).toEqual({
      entities: {
        countries: {
          sweden: {id: 'sweden', name: 'sweden', cities: ['sweden,göteborg', 'sweden,stockholm']},
          finland: {id: 'finland', name: 'finland', cities: ['finland,vasa']},
        },
        cities: {
          'sweden,göteborg': {
            id: 'sweden,göteborg',
            name: 'göteborg',
            parentId: 'sweden',
            addresses: ['sweden,göteborg,kungsgatan'],
          },
          'sweden,stockholm': {
            id: 'sweden,stockholm',
            name: 'stockholm',
            parentId: 'sweden',
            addresses: ['sweden,stockholm,kungsgatan', 'sweden,stockholm,drottninggatan'],
          },
          'finland,vasa': {
            id: 'finland,vasa',
            name: 'vasa',
            parentId: 'finland',
            addresses: ['finland,vasa,kungsgatan'],
          },

        },
        addresses: {
          'sweden,göteborg,kungsgatan': {
            id: 'sweden,göteborg,kungsgatan',
            name: 'kungsgatan',
            parentId: 'sweden,göteborg',
          },
          'sweden,stockholm,drottninggatan': {
            id: 'sweden,stockholm,drottninggatan',
            name: 'drottninggatan',
            parentId: 'sweden,stockholm',
          },
          'sweden,stockholm,kungsgatan': {
            id: 'sweden,stockholm,kungsgatan',
            name: 'kungsgatan',
            parentId: 'sweden,stockholm',
          },
          'finland,vasa,kungsgatan': {
            id: 'finland,vasa,kungsgatan',
            name: 'kungsgatan',
            parentId: 'finland,vasa',
          },
        },
        facilities: {
          a: {id: 'a', name: '1'},
          b: {id: 'b', name: '2'},
          c: {id: 'c', name: '3'},
        },
        secondaryAddresses: {
          aa: {id: 'aa', name: '11'},
          ab: {id: 'ab', name: '12'},
          ac: {id: 'ac', name: '13'},
        },
        gatewaySerials: {
          ba: {id: 'ba', name: '21'},
          bb: {id: 'bb', name: '22'},
          bc: {id: 'bc', name: '23'},
        },
      },
      result: {
        locations: {
          countries: ['sweden', 'finland'],
        },
        alarms: [],
        users: [],
        manufacturers: [],
        productModels: [],
        meterStatuses: [],
        gatewayStatuses: [],
        facilities: ['a', 'b', 'c'],
        secondaryAddresses: ['aa', 'ab', 'ac'],
        gatewaySerials: ['ba', 'bb', 'bc'],
      },
    });
  });
});

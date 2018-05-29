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
      },
    });
  });
});

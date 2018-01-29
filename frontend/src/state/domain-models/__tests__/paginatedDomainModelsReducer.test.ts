import {HasId} from '../../../types/Types';
import {EndPoints, HttpMethod} from '../domainModels';
import {Meter} from '../meter/meterModels';
import {NormalizedPaginated, NormalizedPaginatedState} from '../paginatedDomainModels';
import {requestMethodPaginated} from '../paginatedDomainModelsActions';
import {initialPaginatedDomain, paginatedMeters} from '../paginatedDomainModelsReducer';

describe('paginatedDomainModelsReducer', () => {
  const getRequest = requestMethodPaginated<Meter>(EndPoints.meters, HttpMethod.GET);

  it('appends entities', () => {
    const oldState: NormalizedPaginatedState = initialPaginatedDomain<Meter>();
    expect(Object.keys(oldState.result)).toHaveLength(0);

    const newStateExpected: NormalizedPaginatedState = {
      ...state,
      entities,
      result: {
        [objId]: {...result, isFetching: false},
      },
    };

    const payload: NormalizedPaginated<HasId> = {
        componentId: '234',
        result: {
          content: [1],
          first: true,
          last: true,
          number: 1,
          numberOfElements: 1,
          size: 1,
          sort: null,
          totalElements: 1,
          totalPages: 1,
        },
        entities: {
          meters: {
            1: {
              id: 1,
            }
          }
        },
      }
    ;
    const newState = paginatedMeters(oldState, getRequest.success(payload));
    expect(newState).toEqual(newStateExpected);
  });
});

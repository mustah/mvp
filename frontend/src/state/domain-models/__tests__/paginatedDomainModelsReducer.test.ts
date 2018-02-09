import {ErrorResponse, HasId} from '../../../types/Types';
import {Meter} from '../../domain-models-paginated/meter/meterModels';
import {
  HasPageNumber,
  NormalizedPaginated,
  NormalizedPaginatedState,
} from '../../domain-models-paginated/paginatedDomainModels';
import {requestMethodPaginated} from '../../domain-models-paginated/paginatedDomainModelsActions';
import {initialPaginatedDomain, meters} from '../../domain-models-paginated/paginatedDomainModelsReducer';
import {EndPoints} from '../domainModels';

describe('paginatedDomainModelsReducer', () => {

  describe('meters, paginated', () => {

    const initialState: NormalizedPaginatedState<Meter> = initialPaginatedDomain<Meter>();

    const getRequest =
      requestMethodPaginated<NormalizedPaginated<Meter>>(EndPoints.meters);

    const page = 0;

    const normalizedMeters: NormalizedPaginated<Meter> = {
      page,
      entities: {
        meters: {
          1: {
            id: 1,
            address: {id: 1, name: 'Kungsgatan', cityId: 'got'},
            city: {id: 'got', name: 'Göteborg'},
            position: {latitude: 10, longitude: 10, confidence: 1},
            moid: 123,
            facility: 'torp',
            alarm: '',
            flags: [],
            flagged: false,
            medium: 'Electricity',
            manufacturer: 'ABB',
            statusChangelog: [],
            status: {id: 1, name: 'ok'},
            gatewayId: 1,
            gatewayStatus: {id: 1, name: 'ok'},
            gatewayProductModel: 'Elvaco',
          },
          2: {
            id: 2,
            address: {id: 1, name: 'Kungsgatan', cityId: 'got'},
            city: {id: 'got', name: 'Göteborg'},
            position: {latitude: 10, longitude: 10, confidence: 1},
            moid: 123,
            facility: 'torp',
            alarm: '',
            flags: [],
            flagged: false,
            medium: 'Electricity',
            manufacturer: 'ABB',
            statusChangelog: [],
            status: {id: 1, name: 'ok'},
            gatewayId: 1,
            gatewayStatus: {id: 1, name: 'ok'},
            gatewayProductModel: 'Elvaco',
          },
        },
      },
      result: {
        content: [1, 2],
        totalPages: 1440,
        totalElements: 28800,
        last: false,
        size: 2,
        number: 0,
        first: true,
        numberOfElements: 20,
        sort: null,
      },
    };

    it('has initial state', () => {
      expect(meters(initialState, {type: 'unknown', payload: -1})).toEqual({...initialState});
    });

    it('requests meters', () => {
      const stateAfterRequestInitiation = meters(initialState, getRequest.request(page));
      const expected: NormalizedPaginatedState<Meter> = {
        ...initialState,
        result: {
          [page]: {isFetching: true},
        },
      };
      expect(stateAfterRequestInitiation).toEqual(expected);
    });

    it('adds new meter to state', () => {
      const newState = meters(initialState, getRequest.success(normalizedMeters));
      const expected: NormalizedPaginatedState<Meter> = {
        entities: {...normalizedMeters.entities.meters},
        result: {
          [page]: {
            result: normalizedMeters.result.content,
            isFetching: false,
          },
        },
      };
      expect(newState).toEqual(expected);
    });

    it('appends entities', () => {

      const populatedState: NormalizedPaginatedState<Meter> =
        meters(initialState, getRequest.success(normalizedMeters));

      const anotherPage = 2;

      const payload: NormalizedPaginated<HasId> = {
          page: anotherPage,
          result: {
            content: [1, 4],
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
              1: {id: 1},
              4: {id: 4},
            },
          },
        }
      ;

      const expectedState: NormalizedPaginatedState<HasId> = {
        entities: {...populatedState.entities, 1: {id: 1}, 4: {id: 4}},
        result: {
          ...populatedState.result,
          [anotherPage]: {result: payload.result.content, isFetching: false},
        },
      };

      const newState = meters(populatedState, getRequest.success(payload as NormalizedPaginated<Meter>));
      expect(newState).toEqual(expectedState);
    });

    it('has error when fetching has failed', () => {
      const page = 0;
      const payload: ErrorResponse & HasPageNumber = {message: 'failed', page};

      const stateAfterFailure = meters(initialState, getRequest.failure(payload));

      const failedState = {
        ...initialState,
        result: {
          [page]: {
            error: {message: payload.message},
            isFetching: false,
          },
        },
      };
      expect(stateAfterFailure).toEqual(failedState);
    });
  });
})
;

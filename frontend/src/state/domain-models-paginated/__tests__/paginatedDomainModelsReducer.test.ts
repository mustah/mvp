import {makeMeter} from '../../../__tests__/testDataFactory';
import {EndPoints} from '../../../services/endPoints';
import {ErrorResponse, Identifiable, IdNamed, Status} from '../../../types/Types';
import {SET_SELECTION} from '../../search/selection/selectionActions';
import {Meter} from '../meter/meterModels';
import {
  HasPageNumber,
  NormalizedPaginated,
  NormalizedPaginatedState,
} from '../paginatedDomainModels';
import {clearErrorMeters, paginatedRequestMethod} from '../paginatedDomainModelsActions';
import {
  initialPaginatedDomain,
  meters,
  paginatedDomainModels,
} from '../paginatedDomainModelsReducer';

describe('paginatedDomainModelsReducer', () => {
  const initialState: NormalizedPaginatedState<Meter> = initialPaginatedDomain<Meter>();

  describe('meters, paginated', () => {

    const getRequest = paginatedRequestMethod<NormalizedPaginated<Meter>>(EndPoints.meters);

    const page = 0;

    const status: IdNamed = {id: Status.ok, name: Status.ok};

    const normalizedMeters: NormalizedPaginated<Meter> = {
      page,
      entities: {
        meters: {
          1: {
            id: 1,
            location: {
              address: {id: 1, name: 'Kungsgatan'},
              city: {id: 'got', name: 'Göteborg'},
              position: {latitude: 10, longitude: 10, confidence: 1},
            },
            facility: 'torp',
            alarm: '',
            flags: [],
            flagged: false,
            medium: 'Electricity',
            manufacturer: 'ABB',
            statusChangelog: [],
            status,
            gateway: {id: 1, serial: '123', productModel: 'Elvaco', status},
          },
          2: {
            id: 2,
            location: {
              address: {id: 1, name: 'Kungsgatan'},
              city: {id: 'got', name: 'Göteborg'},
              position: {latitude: 10, longitude: 10, confidence: 1},
            },
            facility: 'torp',
            alarm: '',
            flags: [],
            flagged: false,
            medium: 'Electricity',
            manufacturer: 'ABB',
            statusChangelog: [],
            status,
            gateway: {id: 1, serial: '123-123', productModel: 'Elvaco', status},
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
          [page]: {isFetching: true, isSuccessfullyFetched: false},
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
            isSuccessfullyFetched: true,
          },
        },
      };
      expect(newState).toEqual(expected);
    });

    it('appends entities', () => {

      const populatedState: NormalizedPaginatedState<Meter> =
        meters(initialState, getRequest.success(normalizedMeters));

      const anotherPage = 2;

      const payload: NormalizedPaginated<Identifiable> = {
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

      const expectedState: NormalizedPaginatedState<Identifiable> = {
        entities: {...populatedState.entities, 1: {id: 1}, 4: {id: 4}},
        result: {
          ...populatedState.result,
          [anotherPage]: {
            result: payload.result.content,
            isFetching: false,
            isSuccessfullyFetched: true,
          },
        },
      };

      const newState = meters(
        populatedState,
        getRequest.success(payload as NormalizedPaginated<Meter>),
      );
      expect(newState).toEqual(expectedState);
    });

    it('has error when fetching has failed', () => {
      const page = 0;
      const payload: ErrorResponse & HasPageNumber = {message: 'failed', page};

      const stateAfterFailure = meters(initialState, getRequest.failure(payload));

      const failedState: NormalizedPaginatedState<Identifiable> = {
        ...initialState,
        result: {
          [page]: {
            error: {message: payload.message},
            isFetching: false,
            isSuccessfullyFetched: false,
          },
        },
      };
      expect(stateAfterFailure).toEqual(failedState);
    });
  });

  describe('clear error', () => {
    it('clears error from a page', () => {
      const payload: HasPageNumber = {page: 1};
      const errorState: NormalizedPaginatedState<Meter> = {
        entities: {},
        result: {
          [payload.page]: {
            isSuccessfullyFetched: false,
            isFetching: false,
            error: {message: 'an error'},
            result: [],
          },
        },
      };

      const expected: NormalizedPaginatedState<Meter> = {
        ...errorState,
        result: {[payload.page]: {isFetching: false, isSuccessfullyFetched: false}},
      };
      expect(meters(errorState, clearErrorMeters(payload))).toEqual(expected);

    });
  });

  describe('clear paginatedDomainModels', () => {
    it('clears a cached data', () => {
      expect(paginatedDomainModels(
        {
          meters: {
            ...initialPaginatedDomain<Meter>(),
            entities: {1: {...makeMeter(1, {id: 1, name: 'Mo'}, {id: 1, name: 'b'})}},
          },
        },
        {type: SET_SELECTION, payload: 'irrelevant'},
      )).toEqual({meters: initialPaginatedDomain()});
    });
  });
});

import {makeMeter} from '../../../__tests__/testDataFactory';
import {EndPoints} from '../../../services/endPoints';
import {ErrorResponse, Identifiable, IdNamed, Status} from '../../../types/Types';
import {SET_SELECTION} from '../../search/selection/selectionActions';
import {Gateway} from '../gateway/gatewayModels';
import {clearErrorMeters} from '../meter/meterApiActions';
import {Meter, MetersState} from '../meter/meterModels';
import {HasPageNumber, NormalizedPaginated, NormalizedPaginatedState} from '../paginatedDomainModels';
import {getRequestOf} from '../paginatedDomainModelsActions';
import {getRequestEntityOf} from '../paginatedDomainModelsEntityActions';
import {initialPaginatedDomain, meters, paginatedDomainModels} from '../paginatedDomainModelsReducer';

describe('paginatedDomainModelsReducer', () => {
  const initialState: MetersState = initialPaginatedDomain<Meter>();

  describe('meters, paginated', () => {

    const getRequest = getRequestOf<NormalizedPaginated<Meter>>(EndPoints.meters);

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
      const expected: MetersState = {
        ...initialState,
        result: {
          [page]: {isFetching: true, isSuccessfullyFetched: false},
        },
      };
      expect(stateAfterRequestInitiation).toEqual(expected);
    });

    it('adds new meter to state', () => {
      const newState = meters(initialState, getRequest.success(normalizedMeters));
      const expected: MetersState = {
        isFetchingSingle: false,
        nonExistingSingles: {},
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

      const populatedState: MetersState =
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
        ...populatedState,
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

    it('appends entities if payload is an array', () => {
      const getMeterEntitiesRequest = getRequestEntityOf<Meter>(EndPoints.meters);
      const populatedState: MetersState =
        meters(initialState, getRequest.success(normalizedMeters));

      const payload: Array<Partial<Meter>> = [
        {id: 1},
        {id: 4},
      ];

      const expectedState: NormalizedPaginatedState<Identifiable> = {
        ...populatedState,
        entities: {...populatedState.entities, 1: payload[0] as Meter, 4: payload[1] as Meter},
      };

      const newState = meters(
        populatedState,
        getMeterEntitiesRequest.success(payload as Meter[]),
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
      const errorState: MetersState = {
        isFetchingSingle: false,
        nonExistingSingles: {},
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

      const expected: MetersState = {
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
          gateways: {
            ...initialPaginatedDomain<Gateway>(),
          },
        },
        {type: SET_SELECTION, payload: 'irrelevant'},
      )).toEqual({meters: initialPaginatedDomain(), gateways: initialPaginatedDomain()});
    });
  });
});

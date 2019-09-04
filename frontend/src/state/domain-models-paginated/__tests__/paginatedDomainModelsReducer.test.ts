import {makeMeter, toLocation} from '../../../__tests__/testDataFactory';
import {routes} from '../../../app/routes';
import {RequestParameter} from '../../../helpers/urlFactory';
import {EndPoints} from '../../../services/endPoints';
import {ErrorResponse, Identifiable} from '../../../types/Types';
import {logoutUser} from '../../../usecases/auth/authActions';
import {locationChange} from '../../location/locationActions';
import {search} from '../../search/searchActions';
import {SortOption} from '../../ui/pagination/paginationModels';
import {resetSelection} from '../../user-selection/userSelectionActions';
import {clearErrorMeters} from '../meter/meterApiActions';
import {Meter, MetersState} from '../meter/meterModels';
import {
  NormalizedPaginated,
  NormalizedPaginatedState,
  PageNumbered,
  PaginatedDomainModelsState,
  SingleEntityFailure,
} from '../paginatedDomainModels';
import {makeRequestActionsOf, sortMeters} from '../paginatedDomainModelsActions';
import {makeEntityRequestActionsOf, makePaginatedDeleteRequestActions} from '../paginatedDomainModelsEntityActions';
import {makeInitialState, meters, paginatedDomainModels} from '../paginatedDomainModelsReducer';

describe('paginatedDomainModelsReducer', () => {
  const initialState: MetersState = makeInitialState<Meter>();

  describe('meters, paginated', () => {

    const getRequest = makeRequestActionsOf<NormalizedPaginated<Meter>>(EndPoints.meters);

    const page = 0;

    const location = {
      address: 'Kungsgatan',
      city: 'Göteborg',
      country: 'sverige',
      position: {latitude: 10, longitude: 10},
    };

    const normalizedMeters: NormalizedPaginated<Meter> = {
      page,
      entities: {
        meters: {
          1: {
            id: 1,
            location,
            facility: 'torp',
            medium: 'Electricity',
            manufacturer: 'ABB',
            readIntervalMinutes: 60,
            gatewaySerial: '123',
            organisationId: '',
          },
          2: {
            id: 2,
            location,
            facility: 'torp',
            medium: 'Electricity',
            manufacturer: 'ABB',
            readIntervalMinutes: 60,
            gatewaySerial: '123-123',
            organisationId: '',
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
          [page]: {isFetching: true, isSuccessfullyFetched: false, result: []},
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
      const getMeterEntitiesRequest = makeEntityRequestActionsOf<Meter[]>(EndPoints.meters);
      const populatedState: MetersState = meters(initialState, getRequest.success(normalizedMeters));

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
      const payload: ErrorResponse & PageNumbered = {message: 'failed', page};

      const stateAfterFailure = meters(initialState, getRequest.failure(payload));

      const failedState: NormalizedPaginatedState<Identifiable> = {
        ...initialState,
        result: {
          [page]: {
            error: {message: payload.message},
            isFetching: false,
            isSuccessfullyFetched: false,
            result: [],
          },
        },
      };
      expect(stateAfterFailure).toEqual(failedState);
    });

    describe('delete meter', () => {
      const deleteRequest = makePaginatedDeleteRequestActions<Meter & PageNumbered>(EndPoints.meters);

      it('dispatches delete meter request', () => {
        const state: MetersState = meters(initialState, deleteRequest.request());

        const expectedState: MetersState = {
          ...initialState,
          isFetchingSingle: true,
        };

        expect(state).toEqual(expectedState);
      });

      it('dispatches delete meter failure', () => {
        const failure: SingleEntityFailure = {id: 1, message: 'could not find meter'};

        const state: MetersState = meters(initialState, deleteRequest.failure(failure));

        const expectedState: MetersState = {
          ...initialState,
          nonExistingSingles: {1: failure},
        };

        expect(state).toEqual(expectedState);
      });

      it('removes meter with id', () => {
        const meter1: Meter = makeMeter(1, 'stockholm', 'king street');
        const meter2: Meter = makeMeter(2, 'stockholm', 'king street');

        const initialState: MetersState = {
          isFetchingSingle: false,
          nonExistingSingles: {},
          entities: {
            1: {...meter1},
            2: {...meter2}
          },
          result: {
            [page]: {
              result: [1, 2],
              isFetching: false,
              isSuccessfullyFetched: true,
            },
          },
        };

        const state: MetersState = meters(initialState, deleteRequest.success({...meter1, page}));

        const expectedState: MetersState = {
          ...initialState,
          entities: {2: {...meter2}},
          result: {
            ...initialState.result,
            [page]: {
              ...initialState.result[page],
              result: [2],
            }
          }
        };

        expect(state).toEqual(expectedState);
      });
    });

  });

  describe('clear error', () => {
    it('clears error from a page', () => {
      const payload: PageNumbered = {page: 1};
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
        result: {[payload.page]: {isFetching: false, isSuccessfullyFetched: false, result: []}},
      };
      expect(meters(errorState, clearErrorMeters(payload))).toEqual(expected);
    });
  });

  describe('clear paginatedDomainModels', () => {

    it('clears a cached data', () => {
      const expectedState: PaginatedDomainModelsState = {
        batchReferences: makeInitialState(),
        devices: makeInitialState(),
        meters: initialState,
        gateways: makeInitialState(),
        collectionStatFacilities: makeInitialState(),
        meterCollectionStatFacilities: makeInitialState(),
      };

      const state: PaginatedDomainModelsState = paginatedDomainModels(
        {
          meters: {
            ...initialState,
            entities: {1: {...makeMeter(1, 'stockholm', 'king street')}},
          },
          batchReferences: makeInitialState(),
          devices: makeInitialState(),
          gateways: makeInitialState(),
          collectionStatFacilities: makeInitialState(),
          meterCollectionStatFacilities: makeInitialState()
        },
        resetSelection()
      );

      expect(state).toEqual(expectedState);
    });
  });

  describe('sortMeters', () => {

    it('can start sorting', () => {
      const state: MetersState = initialState;

      const payload: SortOption[] = [{field: RequestParameter.city}];
      const newState: MetersState = meters(state, sortMeters(payload));

      expect(newState).toHaveProperty('sort', payload);
    });

    it('can remove sorting', () => {
      const state: MetersState = meters(initialState, sortMeters([{field: RequestParameter.city}]));

      const newState: MetersState = meters(state, sortMeters(undefined));

      expect(newState).not.toHaveProperty('sort');
    });

    it('keeps the result when the sorting is unchanged', () => {
      const payload: SortOption[] = [{field: RequestParameter.city}];
      const state: MetersState = meters(
        {
          ...initialState,
          result: {
            1: {
              isFetching: false,
              isSuccessfullyFetched: true,
              result: [123, 456],
            },
          },
          sort: [...payload],
        },
        sortMeters(payload)
      );

      expect(state).toHaveProperty('result');
      expect(state.result).toHaveProperty('1');
      expect(state.result[1]).toHaveProperty('result', [123, 456]);
    });

    it('throws out the result when the sorting changes', () => {
      const payload: SortOption[] = [{field: RequestParameter.city}];
      const differentPayload: SortOption[] = [{field: RequestParameter.city, dir: 'DESC'}];
      const state: MetersState = meters(
        {
          ...initialState,
          result: {
            1: {
              isFetching: false,
              isSuccessfullyFetched: true,
              result: [123, 456],
            },
          },
          sort: [...payload],
        },
        sortMeters(differentPayload)
      );

      expect(state).toHaveProperty('result');
      expect(state.result).not.toHaveProperty('1');
    });

    it('can replace sorting', () => {
      const state: MetersState = meters(initialState, sortMeters([{field: RequestParameter.city}]));

      const newState: MetersState = meters(
        state,
        sortMeters([{field: RequestParameter.city}, {field: RequestParameter.address}])
      );

      expect(newState).toHaveProperty(
        'sort',
        [
          {field: RequestParameter.city},
          {field: RequestParameter.address}
        ]
      );
    });

    it('resets state when user makes a new search', () => {
      const state: MetersState = {
        ...initialState,
        result: {
          1: {
            isFetching: false,
            isSuccessfullyFetched: true,
            result: [123, 456],
          },
        },
      };

      const newState: MetersState = meters(state, search({validation: {query: 'evo'}}));

      expect(newState).toEqual(initialState);
    });

    it('keeps state when user navigates to another page than search page', () => {
      const state: MetersState = {
        ...initialState,
        result: {
          1: {
            isFetching: false,
            isSuccessfullyFetched: true,
            result: [123, 456],
          },
        },
        sort: [{field: RequestParameter.city}],
      };

      const newState: MetersState = meters(state, locationChange(toLocation(routes.dashboard)));

      expect(newState).toBe(state);
    });

  });

  describe('logout user', () => {

    it('resets state to initial state', () => {
      const prevState: PaginatedDomainModelsState = {
        meters: {...initialState, isFetchingSingle: true},
        batchReferences: {...makeInitialState(), isFetchingSingle: true},
        devices: {...makeInitialState(), isFetchingSingle: true},
        gateways: {...makeInitialState(), isFetchingSingle: true},
        collectionStatFacilities: {...makeInitialState(), isFetchingSingle: true},
        meterCollectionStatFacilities: {...makeInitialState(), isFetchingSingle: true},
      };

      const expected: PaginatedDomainModelsState = {
        meters: initialState,
        batchReferences: makeInitialState(),
        devices: makeInitialState(),
        gateways: makeInitialState(),
        collectionStatFacilities: makeInitialState(),
        meterCollectionStatFacilities: makeInitialState(),
      };

      const state = paginatedDomainModels(prevState, logoutUser(undefined));

      expect(state).toEqual(expected);
    });
  });

});

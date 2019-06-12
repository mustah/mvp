import {makeCollectionStat, makeMeter} from '../../../__tests__/testDataFactory';
import {Maybe} from '../../../helpers/Maybe';
import {ErrorResponse} from '../../../types/Types';
import {CollectionStat, CollectionStatFacilityState} from '../../domain-models/collection-stat/collectionStatModels';
import {paginationPageSize} from '../../ui/pagination/paginationReducer';
import {Meter, MetersState} from '../meter/meterModels';
import {PaginatedResult} from '../paginatedDomainModels';
import {makeInitialState} from '../paginatedDomainModelsReducer';
import {
  fillWithNull,
  getAllMeters,
  getCollectionStats,
  getFirstPageError,
  getPageError,
  PageState
} from '../paginatedDomainModelsSelectors';

describe('paginatedDomainModelsSelectors', () => {

  const paginatedResult: PaginatedResult = {isFetching: false, isSuccessfullyFetched: true, result: []};

  describe('getAllMeters', () => {
    const initialState: MetersState = makeInitialState<Meter>();

    const meter1: Meter = makeMeter(1, 'stockholm', 'king street');
    const meter2: Meter = makeMeter(2, 'stockholm', 'king street');
    const meter3: Meter = makeMeter(3, 'stockholm', 'king street');

    it('has no meters when empty state', () => {
      expect(getAllMeters(initialState)).toEqual([]);
    });

    it('has one meter', () => {
      const meters: MetersState = {
        ...initialState,
        entities: {[meter1.id]: meter1},
        result: {
          [0]: {...paginatedResult, result: [meter1.id]},
        },
      };

      expect(getAllMeters(meters)).toEqual([meter1]);
    });

    it('has more than one meter', () => {
      const meters: MetersState = {
        ...initialState,
        entities: {[meter1.id]: meter1, [meter2.id]: meter2, [meter3.id]: meter3},
        result: {
          [0]: {...paginatedResult, result: [meter1.id, meter2.id, meter3.id]},
        },
      };

      expect(getAllMeters(meters)).toEqual([meter1, meter2, meter3]);
    });

    it('can get all meters from all pages', () => {
      const meters: MetersState = {
        ...initialState,
        entities: {[meter1.id]: meter1, [meter2.id]: meter2, [meter3.id]: meter3},
        result: {
          [0]: {...paginatedResult, result: [meter1.id, meter2.id]},
          [1]: {...paginatedResult, result: [meter3.id]},
        },
      };

      expect(getAllMeters(meters)).toEqual([meter1, meter2, meter3]);
    });

    it('fills the given page only if not already fetched', () => {
      const meters: MetersState = {
        ...initialState,
        entities: {[meter1.id]: meter1, [meter2.id]: meter2},
        result: {
          [1]: {...paginatedResult, result: [meter1.id, meter2.id]},
        },
      };

      const expected: Array<Meter | null> = [
        ...fillWithNull({page: 1, fillSize: paginationPageSize}),
        meter1,
        meter2
      ];

      expect(getAllMeters(meters)).toEqual(expected);
    });

    it('fills from start to existing page', () => {
      const meters: MetersState = {
        ...initialState,
        entities: {[meter1.id]: meter1, [meter2.id]: meter2, [meter3.id]: meter3},
        result: {
          [3]: {...paginatedResult, result: [meter1.id]},
          [4]: {...paginatedResult, result: [meter2.id]},
          [5]: {...paginatedResult, result: [meter3.id]},
        },
      };

      const expected: Array<Meter | null> = [
        ...fillWithNull({page: 3, fillSize: paginationPageSize}),
        meter1,
        meter2,
        meter3
      ];

      expect(getAllMeters(meters)).toEqual(expected);
    });

    it('can handle empty page', () => {
      const meters: MetersState = {
        ...initialState,
        entities: {[meter1.id]: meter1, [meter2.id]: meter2},
        result: {
          [0]: {...paginatedResult, result: [meter1.id, meter2.id]},
          [1]: {...paginatedResult},
        },
      };

      expect(getAllMeters(meters)).toEqual([meter1, meter2]);
    });

    describe('getPageError', () => {

      it('uses memoized instance when input is the same', () => {
        const meters: MetersState = {
          ...initialState,
          entities: {[meter1.id]: meter1, [meter2.id]: meter2},
          result: {
            [0]: {...paginatedResult, result: [meter1.id, meter2.id]},
            [1]: {...paginatedResult},
          },
        };
        const pageState: PageState<Meter> = {page: 6, state: meters};

        const pageError1 = getPageError(pageState);
        const pageError2 = getPageError(pageState);

        expect(pageError2).toBe(pageError1);
        expect(pageError1).toEqual(Maybe.nothing());
      });

      it('uses does not memoize when input is not the same', () => {
        const error: ErrorResponse = {message: 'Error'};
        const meters: MetersState = {
          ...initialState,
          entities: {[meter1.id]: meter1, [meter2.id]: meter2},
          result: {
            [0]: {...paginatedResult, result: [meter1.id, meter2.id]},
            [1]: {...paginatedResult, error},
          },
        };
        const pageState1: PageState<Meter> = {page: 1, state: meters};
        const pageState2: PageState<Meter> = {page: 7, state: meters};

        const pageError1 = getPageError(pageState1);
        const pageError2 = getPageError(pageState2);

        expect(pageError1).not.toBe(pageError2);
        expect(pageError1).toEqual(Maybe.maybe(error));
        expect(pageError2).toEqual(Maybe.nothing());
      });
    });

    describe('getFirstPageError', () => {

      it('returns nothing when there are no errors', () => {
        const meters: MetersState = {
          ...initialState,
          entities: {[meter1.id]: meter1, [meter2.id]: meter2},
          result: {
            [0]: {...paginatedResult, result: [meter1.id]},
            [1]: {...paginatedResult, result: [meter1.id]},
          },
        };

        expect(getFirstPageError(meters)).toEqual(Maybe.nothing());
      });

      it('gets the first page with error', () => {
        const error: ErrorResponse = {message: 'Error'};
        const error2: ErrorResponse = {message: 'Error2'};
        const meters: MetersState = {
          ...initialState,
          entities: {[meter1.id]: meter1, [meter2.id]: meter2},
          result: {
            [0]: {...paginatedResult, result: [meter1.id]},
            [1]: {...paginatedResult, result: [meter1.id]},
            [2]: {...paginatedResult, error: error2},
            [3]: {...paginatedResult, error},
          },
        };

        expect(getFirstPageError(meters)).toEqual(Maybe.maybe(error2));
      });
    });

  });

  describe('get all collections', () => {
    const initialState: CollectionStatFacilityState = makeInitialState<CollectionStat>();

    const collectionStat1: CollectionStat = makeCollectionStat(1, 'a');
    const collectionStat2: CollectionStat = makeCollectionStat(2, 'b');

    it('has no collections when state is empty', () => {
      expect(getCollectionStats(initialState)).toEqual([]);
    });

    it('has one meter', () => {
      const state: CollectionStatFacilityState = {
        ...initialState,
        entities: {[collectionStat1.id]: collectionStat1},
        result: {
          [0]: {...paginatedResult, result: [collectionStat1.id]},
        },
      };

      expect(getCollectionStats(state)).toEqual([collectionStat1]);
    });

    it('fills from start to existing page', () => {
      const meters: CollectionStatFacilityState = {
        ...initialState,
        entities: {
          [collectionStat1.id]: collectionStat1,
          [collectionStat2.id]: collectionStat2,
        },
        result: {
          [3]: {...paginatedResult, result: [collectionStat1.id]},
          [4]: {...paginatedResult, result: [collectionStat2.id]},
        },
      };

      const expected: Array<CollectionStat | null> = [
        ...fillWithNull({page: 3, fillSize: paginationPageSize}),
        collectionStat1,
        collectionStat2,
      ];

      expect(getCollectionStats(meters)).toEqual(expected);
    });
  });
});

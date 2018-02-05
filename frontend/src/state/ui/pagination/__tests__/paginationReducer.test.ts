import {paginationRequestPage, paginationUpdateMetaData} from '../paginationActions';
import {PaginationMetadata, PaginationState, PaginationMetadataPayload, PaginationChangePayload} from '../paginationModels';
import {limit, pagination} from '../paginationReducer';

describe('paginationReducer', () => {

  const paginatedState: Readonly<PaginationState> = {
    test: {
      first: false,
      last: false,
      currentPage: 4,
      requestedPage: 4,
      numberOfElements: 10,
      size: limit,
      sort: null,
      totalElements: 200,
      totalPages: 20,
    },
  };

  describe('pagination meta data', () => {

    const paginationData: Readonly<PaginationMetadata> = {
      first: false,
      last: false,
      currentPage: 2,
      requestedPage: 2,
      numberOfElements: 10,
      size: limit,
      sort: null,
      totalElements: 200,
      totalPages: 20,
    };

    it('has initial state', () => {
      expect(pagination({}, {type: 'unknown'})).toEqual({});
    });

    it('updates pagination meta data for a component', () => {
      const payload: PaginationMetadataPayload = {page: paginationData, componentId: 'test'};

      expect(pagination(paginatedState, paginationUpdateMetaData(payload))).toEqual({
        ...paginatedState,
        test: {...paginationData},
      });
    });
    it('only updates the targeted component and leave the others untouched', () => {
      const payload: PaginationMetadataPayload = {page: paginationData, componentId: 'test2'};

      expect(pagination(paginatedState, paginationUpdateMetaData(payload))).toEqual({
        ...paginatedState,
        test2: {...paginationData},
      });
    });
  });
  describe('pagination requestedPage', () => {
    it('changes requestedPage', () => {
      const payload: PaginationChangePayload = {
        componentId: 'test',
        page: 10,
      };

      expect(pagination({}, paginationRequestPage(payload))).toEqual({
        test: {requestedPage: 10},
      });

      expect(pagination(paginatedState, paginationRequestPage(payload))).toEqual({
        test: {...paginatedState.test, requestedPage: 10},
      });
    });
    it('only changes requestedPage for the targeted component', () => {
      const payload: PaginationChangePayload = {
        componentId: 'test2',
        page: 10,
      };

      expect(pagination(paginatedState, paginationRequestPage(payload))).toEqual({
        ...paginatedState,
        test2: {requestedPage: 10},
      });
    });
  });
});

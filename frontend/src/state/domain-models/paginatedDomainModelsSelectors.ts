import {uuid} from '../../types/Types';
import {NormalizedPaginatedState} from './paginatedDomainModels';

export const getPaginatedResultDomainModels =
  (state: NormalizedPaginatedState, componentId: uuid): uuid[] =>
    state.result[componentId].content;

import {createSelector} from 'reselect';
import {identity} from '../../../helpers/commonHelpers';
import {Maybe} from '../../../helpers/Maybe';
import {Pagination, PaginationLookupState} from './paginationModels';
import {initialState} from './paginationReducer';

export const getPagination =
  createSelector<PaginationLookupState, PaginationLookupState, Pagination>(
    identity,
    state => Maybe.maybe(state)
      .map(it => it.pagination[it.entityType])
      .orElseGet(() => initialState[state.entityType]),
  );

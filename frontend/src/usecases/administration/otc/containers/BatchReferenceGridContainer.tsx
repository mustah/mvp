import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {DispatchToProps, StateToProps} from '../../../../components/infinite-list/InfiniteList';
import {RootState} from '../../../../reducers/rootReducer';
import {fetchBatchReferences} from '../../../../state/domain-models-paginated/batch-references/batchReferenceApiActions';
import {BatchReference} from '../../../../state/domain-models-paginated/batch-references/batchReferenceModels';
import {sortBatchReferences} from '../../../../state/domain-models-paginated/paginatedDomainModelsActions';
import {
  getBatchReferences,
  getPageIsFetching
} from '../../../../state/domain-models-paginated/paginatedDomainModelsSelectors';
import {changePage} from '../../../../state/ui/pagination/paginationActions';
import {Pagination} from '../../../../state/ui/pagination/paginationModels';
import {getBatchReferencesParameters} from '../../../../state/user-selection/userSelectionSelectors';
import {BatchReferenceGrid} from '../components/BatchReferenceGrid';

const mapStateToProps = (rootState: RootState): StateToProps<BatchReference> => {
  const {
    paginatedDomainModels: {batchReferences},
    ui: {pagination: paginationState},
  }: RootState = rootState;

  const pagination: Pagination = paginationState.batchReferences;
  const {page, totalElements} = pagination;
  const {sort} = batchReferences;
  const isFetching = getPageIsFetching(batchReferences, page);

  return ({
    entityType: 'batchReferences',
    hasContent: isFetching || totalElements > 0,
    isFetching,
    items: getBatchReferences(batchReferences),
    parameters: getBatchReferencesParameters(rootState),
    pagination,
    sort,
  });
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  changePage,
  fetchPaginated: fetchBatchReferences,
  sortTable: sortBatchReferences,
}, dispatch);

export const BatchReferenceGridContainer = connect(mapStateToProps, mapDispatchToProps)(BatchReferenceGrid);

import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history4/redirect';
import {RootState} from '../../../reducers/index';
import {PageContainer} from '../../common/components/layouts/layout/PageLayout';
import {ProblemOverview} from '../../common/components/problem-overview/ProblemOverview';
import {SelectionDropdown} from '../../common/components/selection-dropdown/SelectionDropdown';
import {collectionAddFilter, collectionRemoveFilter, fetchCollections, fetchGateways} from '../collectionActions';
import {ChosenFilter} from '../components/chosen-filter/ChosenFilter';
import {CollectionOverview} from '../components/CollectionOverview';
import {Category, CollectionState, Pagination} from '../models/Collections';
import CollectionTabsContainer from './CollectionTabsContainer';

export interface CollectionContainerProps {
  fetchCollections: () => any;
  fetchGateways: (filter, page: number, limit: number) => any;
  collection: CollectionState;
  categories: Category;
  filterAction: (filter) => any;
  filterDelete: (something) => any;
  pagination: Pagination;
}

class CollectionContainer extends React.Component<CollectionContainerProps & InjectedAuthRouterProps, any> {
  componentDidMount() {
    const {pagination: {page, limit}} = this.props;
    this.props.fetchCollections();
    this.props.fetchGateways(this.props.collection.filter, page, limit);
  }

  render() {
    const {categories, filterAction, filterDelete, collection: {filter}} = this.props;

    return (
      <PageContainer>
        <CollectionOverview/>
        <SelectionDropdown filterAction={filterAction}/>
        <ProblemOverview categories={categories}/>
        <ChosenFilter onDelete={filterDelete} filter={filter}/>
        <CollectionTabsContainer/>
      </PageContainer>
    );
  }
}

const mapStateToProps = (state: RootState) => {
  const {collection} = state;

  return {
    collection,
    categories: collection.categories,
    pagination: collection.pagination,
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
  fetchCollections,
  fetchGateways,
  filterAction: collectionAddFilter,
  filterDelete: collectionRemoveFilter,
}, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(CollectionContainer);

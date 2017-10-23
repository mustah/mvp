import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history4/redirect';
import {RootState} from '../../../reducers/index';
import {translate} from '../../../services/translationService';
import {PeriodSelection} from '../../common/components/dates/PeriodSelection';
import {PageContainer} from '../../common/components/layouts/layout/PageLayout';
import {Row} from '../../common/components/layouts/row/Row';
import {ProblemOverview} from '../../common/components/problem-overview/ProblemOverview';
import {MainTitle} from '../../common/components/texts/Title';
import {SearchParameter} from '../../search/models/searchModels';
import {toggleSearchOption} from '../../search/searchActions';
import {SearchState} from '../../search/searchReducer';
import {collectionAddFilter, collectionRemoveFilter, fetchCollections, fetchGateways} from '../collectionActions';
import {ChosenFilter} from '../components/chosen-filter/ChosenFilter';
import {Category, CollectionState, Pagination} from '../models/Collections';
import CollectionTabsContainer from './CollectionTabsContainer';

interface DispatchToProps {
  fetchCollections: () => void;
  fetchGateways: (filter, page: number, limit: number) => void;
  filterAction: (filter) => void;
  filterDelete: (something, value) => void;
  selectSearchOption: (searchParameters: SearchParameter) => void;
}

interface StateToProps {
  search: SearchState;
  collection: CollectionState;
  categories: Category;
  pagination: Pagination;
}

type Props = StateToProps & DispatchToProps & InjectedAuthRouterProps;

class CollectionContainer extends React.Component<Props> {

  componentDidMount() {
    const {pagination: {page, limit}} = this.props;
    this.props.fetchCollections();
    this.props.fetchGateways(this.props.collection.filter, page, limit);
  }

  render() {
    const {categories, filterAction, filterDelete, collection: {filter}, search, selectSearchOption} = this.props;

    return (
      <PageContainer>
        <Row className="space-between">
          <MainTitle>{translate('collection')}</MainTitle>
          <PeriodSelection/>
        </Row>

        <ProblemOverview
          search={search}
          categories={categories}
          selectSearchOption={selectSearchOption}
          filterAction={filterAction}
        />
        <ChosenFilter onDelete={filterDelete} filter={filter}/>
        <CollectionTabsContainer/>
      </PageContainer>
    );
  }
}

const mapStateToProps = (state: RootState) => {
  const {collection, search} = state;
  const {categories, pagination} = collection;
  return {
    collection,
    categories,
    pagination,
    search,
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
  fetchCollections,
  fetchGateways,
  filterAction: collectionAddFilter,
  filterDelete: collectionRemoveFilter,
  selectSearchOption: toggleSearchOption,
}, dispatch);

export default connect<StateToProps, DispatchToProps, {}>(mapStateToProps, mapDispatchToProps)(CollectionContainer);

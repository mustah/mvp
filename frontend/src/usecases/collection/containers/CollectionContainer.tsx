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
import {SelectionParameter} from '../../../state/search/selection/selectionModels';
import {toggleSelection} from '../../../state/search/selection/selectionActions';
import {SelectionState} from '../../../state/search/selection/selectionReducer';
import {collectionAddFilter, collectionRemoveFilter, fetchCollections, fetchGateways} from '../collectionActions';
import {ChosenFilter} from '../components/chosen-filter/ChosenFilter';
import {Category, CollectionState, Pagination} from '../models/Collections';
import CollectionTabsContainer from './CollectionTabsContainer';

interface DispatchToProps {
  fetchCollections: () => void;
  fetchGateways: (filter, page: number, limit: number) => void;
  filterAction: (filter) => void;
  filterDelete: (something, value) => void;
  toggleSearchOption: (searchParameters: SelectionParameter) => void;
}

interface StateToProps {
  selection: SelectionState;
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
    const {categories, filterAction, filterDelete, collection: {filter}, selection, toggleSearchOption} = this.props;

    return (
      <PageContainer>
        <Row className="space-between">
          <MainTitle>{translate('collection')}</MainTitle>
          <PeriodSelection/>
        </Row>

        <ProblemOverview
          selection={selection}
          categories={categories}
          toggleSearchOption={toggleSearchOption}
          filterAction={filterAction}
        />
        <ChosenFilter onDelete={filterDelete} filter={filter}/>
        <CollectionTabsContainer/>
      </PageContainer>
    );
  }
}

const mapStateToProps = (state: RootState) => {
  const {collection, selection} = state;
  const {categories, pagination} = collection;
  return {
    collection,
    categories,
    pagination,
    selection,
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
  fetchCollections,
  fetchGateways,
  filterAction: collectionAddFilter,
  filterDelete: collectionRemoveFilter,
  toggleSearchOption: toggleSelection,
}, dispatch);

export default connect<StateToProps, DispatchToProps, {}>(mapStateToProps, mapDispatchToProps)(CollectionContainer);

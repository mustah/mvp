import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history4/redirect';
import {RootState} from '../../../reducers/index';
import {translate} from '../../../services/translationService';
import {Column} from '../../common/components/layouts/column/Column';
import {Content} from '../../common/components/layouts/content/Content';
import {Layout} from '../../common/components/layouts/layout/Layout';
import {ProblemOverview} from '../../common/components/problem-overview/ProblemOverview';
import {SelectionDropdown} from '../../common/components/selection-dropdown/SelectionDropdown';
import {SelectionOverview} from '../../common/components/selection-overview/SelectionOverview';
import {collectionAddFilter, collectionRemoveFilter, fetchCollections, fetchGateways} from '../collectionActions';
import {ChosenFilter} from '../components/chosen-filter/ChosenFilter';
import {CollectionOverview} from '../components/CollectionOverview';
import {Category, CollectionState} from '../models/Collections';
import CollectionTabsContainer from './CollectionTabsContainer';

export interface CollectionContainerProps {
  fetchCollections: () => any;
  fetchGateways: (filter) => any;
  collection: CollectionState;
  categories: Category;
  filterAction: (filter) => any;
  filterDelete: (something) => any;
}

class CollectionContainer extends React.Component<CollectionContainerProps & InjectedAuthRouterProps, any> {
  componentDidMount() {
    this.props.fetchCollections();
    this.props.fetchGateways(this.props.collection.filter);
  }

  render() {
    const {categories, filterAction, filterDelete, collection: {filter}} = this.props;

    return (
      <Layout>
        <Column className="flex-1">
          <SelectionOverview title={translate('all')}/>
          <Content>
            <CollectionOverview/>
            <SelectionDropdown filterAction={filterAction}/>
            <ProblemOverview categories={categories}/>
            <ChosenFilter onDelete={filterDelete} filter={filter}/>
            <CollectionTabsContainer/>
          </Content>
        </Column>
      </Layout>
    );
  }
}

const mapStateToProps = (state: RootState) => {
  const {collection} = state;

  return {
    collection,
    categories: collection.categories,
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
  fetchCollections,
  fetchGateways,
  filterAction: collectionAddFilter,
  filterDelete: collectionRemoveFilter,
}, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(CollectionContainer);

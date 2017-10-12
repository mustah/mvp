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
import {SelectionOverview} from '../../common/components/selection-overview/SelectionOverview';
import {collectionSetFilter, fetchCollections, fetchGateways} from '../collectionActions';
import {CollectionOverview} from '../components/CollectionOverview';
import {Category, CollectionState} from '../models/Collections';
import CollectionTabsContainer from './CollectionTabsContainer';
import {SelectionDropdown} from '../../common/components/selection-dropdown/SelectionDropdown';

export interface CollectionContainerProps {
  fetchCollections: () => any;
  fetchGateways: () => any;
  collection: CollectionState;
  categories: Category;
  collectionSetFilter: (filter) => any;
}

class CollectionContainer extends React.Component<CollectionContainerProps & InjectedAuthRouterProps, any> {
  componentDidMount() {
    this.props.fetchCollections();
    this.props.fetchGateways();
  }

  render() {
    const {categories, collectionSetFilter} = this.props;

    return (
      <Layout>
        <Column className="flex-1">
          <SelectionOverview title={translate('all')}/>
          <Content>
            <CollectionOverview/>
            <SelectionDropdown setFilter={collectionSetFilter}/>
            <ProblemOverview categories={categories}/>
            <CollectionTabsContainer />
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
  collectionSetFilter,
}, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(CollectionContainer);

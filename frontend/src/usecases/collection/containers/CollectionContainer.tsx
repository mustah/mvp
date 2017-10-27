import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history4/redirect';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {fetchGateways} from '../../../state/domain-models/gateway/gatewayActions';
import {toggleSelection} from '../../../state/search/selection/selectionActions';
import {SelectionParameter} from '../../../state/search/selection/selectionModels';
import {SelectionState} from '../../../state/search/selection/selectionReducer';
import {PeriodSelection} from '../../common/components/dates/PeriodSelection';
import {PageContainer} from '../../common/containers/PageContainer';
import {Row} from '../../common/components/layouts/row/Row';
import {MainTitle} from '../../common/components/texts/Title';
import {fetchCollections} from '../collectionActions';
import {Category, CollectionState, Pagination} from '../models/Collections';
import CollectionTabsContainer from './CollectionTabsContainer';

interface DispatchToProps {
  fetchCollections: () => void;
  fetchGateways: (filter) => void;
  toggleSearchOption: (searchParameters: SelectionParameter) => void;
}

interface StateToProps {
  selection: SelectionState;
  collection: CollectionState;
  categories: Category;
  pagination: Pagination;
}

type Props = StateToProps & DispatchToProps & InjectedAuthRouterProps ;

class CollectionContainer extends React.Component<Props> {

  componentDidMount() {
    this.props.fetchCollections();
    this.props.fetchGateways(this.props.collection.filter); // TODO: Only fetch if needed.
  }

  render() {
    return (
      <PageContainer>
        <Row className="space-between">
          <MainTitle>{translate('collection')}</MainTitle>
          <PeriodSelection/>
        </Row>

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
  toggleSearchOption: toggleSelection,
}, dispatch);

export default connect<StateToProps, DispatchToProps, {}>(mapStateToProps, mapDispatchToProps)(CollectionContainer);

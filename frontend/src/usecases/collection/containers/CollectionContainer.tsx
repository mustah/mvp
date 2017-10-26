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
import {PageContainer} from '../../common/components/layouts/layout/PageLayout';
import {Row} from '../../common/components/layouts/row/Row';
import {PieChartSelector, PieClick} from '../../common/components/pie-chart-selector/PieChartSelector';
import {MainTitle} from '../../common/components/texts/Title';
import {fetchCollections} from '../collectionActions';
import {Category, CollectionState, Pagination} from '../models/Collections';
import CollectionTabsContainer from './CollectionTabsContainer';
import {uuid} from '../../../types/Types';

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

type Props = StateToProps & DispatchToProps & InjectedAuthRouterProps;

class CollectionContainer extends React.Component<Props> {

  componentDidMount() {
    this.props.fetchCollections();
    this.props.fetchGateways(this.props.collection.filter);
  }

  render() {
    const cities = [
      {name: 'Älmhult', value: 822},
      {name: 'Perstorp', value: 893},
    ];

    // TODO the city is maybe not an uuid here, but a string.. yikes
    const selectCity: PieClick = (city: uuid) => alert('You selected the city ' + city);

    const productModels = [
      {name: 'CMe2100', value: 66},
      {name: 'CMi2110', value: 1649},
    ];

    // TODO the city is maybe not an uuid here, but a string.. yikes
    const selectProductModel: PieClick =
      (productModel: uuid) => alert('You selected the product model ' + productModel);

    /**
     * We want the pie charts to differentiate against each other
     * We can use a service like https://www.sessions.edu/color-calculator/
     * to find sets of "splít complimentary", "triadic" or "tetriadic" colors.
     */
    const colors: [string[]] = [
      ['#E8A090', '#FCE8CC'],
      ['#588E95', '#CCD9CE'],
    ];

    return (
      <PageContainer>
        <Row className="space-between">
          <MainTitle>{translate('collection')}</MainTitle>
          <PeriodSelection/>
        </Row>

        <PieChartSelector onClick={selectCity} data={cities} colors={colors[0]}/>
        <PieChartSelector onClick={selectProductModel} data={productModels} colors={colors[1]}/>

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

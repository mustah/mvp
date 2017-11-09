import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {Gateway} from '../../../state/domain-models/gateway/gatewayModels';
import {getGatewayEntities, getGatewaysTotal} from '../../../state/domain-models/gateway/gatewaySelectors';
import {changeTabCollection, changeTabOptionCollection} from '../../../state/ui/tabs/tabsActions';
import {getSelectedTab, getTabs} from '../../../state/ui/tabs/tabsSelectors';
import {uuid} from '../../../types/Types';
import {Row, RowCenter} from '../../common/components/layouts/row/Row';
import {PaginationControl} from '../../common/components/pagination-control/PaginationControl';
import {PieChartSelector} from '../../common/components/pie-chart-selector/PieChartSelector';
import {Tab} from '../../common/components/tabs/components/Tab';
import {TabContent} from '../../common/components/tabs/components/TabContent';
import {TabHeaders} from '../../common/components/tabs/components/TabHeaders';
import {TabOption} from '../../common/components/tabs/components/TabOption';
import {TabOptions} from '../../common/components/tabs/components/TabOptions';
import {Tabs} from '../../common/components/tabs/components/Tabs';
import {TabSettings} from '../../common/components/tabs/components/TabSettings';
import {TabTopBar} from '../../common/components/tabs/components/TabTopBar';
import {TabsContainerProps, tabType} from '../../common/components/tabs/models/TabsModel';
import {Bold} from '../../common/components/texts/Texts';
import MapContainer, {PopupMode} from '../../map/containers/MapContainer';
import {changePaginationCollection} from '../../../state/ui/pagination/paginationActions';
import {Pagination} from '../../../state/ui/pagination/paginationModels';
import {getCollectionPagination, getPaginationList} from '../../../state/ui/pagination/paginationSelectors';
import {GatewayList} from '../components/GatewayList';

interface CollectionTabsContainer extends TabsContainerProps {
  numOfGateways: number;
  gateways: { [key: string]: Gateway };
  paginatedList: uuid[];
  pagination: Pagination;
  paginationChangePage: (page: number) => any;
}

const CollectionTabsContainer = (props: CollectionTabsContainer) => {
  const {
    selectedTab,
    changeTab,
    gateways,
    pagination,
    paginationChangePage,
    paginatedList,
    numOfGateways,
    changeTabOption,
    tabs,
  } = props;

  const cities = [
    {name: 'Älmhult', value: 822},
    {name: 'Perstorp', value: 893},
  ];

  const productModels = [
    {name: 'CMe2100', value: 66},
    {name: 'CMi2110', value: 1649},
  ];

  const statuses = [
    {name: translate('ok'), value: 1713},
    {name: translate('reported'), value: 2},
    {name: translate('unhandled problems'), value: 0},
  ];

  const colors: [string[]] = [
    ['#e8a090', '#fce8cc'],
    ['#588e95', '#ccd9ce'],
    ['#b7e000', '#f7be29', '#ed4200'],
  ];

  const faultsPerCity = [
    {name: 'Älmhult', value: 1},
    {name: 'Perstorp', value: 1},
  ];

  const faultsPerProductModel = [
    {name: 'CMi2110', value: 2},
  ];

  const numberOfGateways = cities.reduce((sum, tuple) => sum + tuple.value, 0);
  const numberOfFaults = faultsPerCity.reduce((sum, tuple) => sum + tuple.value, 0);
  const numberOfUnhandledFaults = 0;

  const graphTabContents = ((tabName: string): any => {
    if (tabName === 'population') {
      return (
        <div>
          <Row>
            <p>Antal gateways: <Bold>{numberOfGateways}</Bold>.</p>
          </Row>
          <RowCenter>
            <PieChartSelector heading="Städer" data={cities} colors={colors[0]}/>
            <PieChartSelector heading="Produktmodeller" data={productModels} colors={colors[1]}/>
            <PieChartSelector heading="Status" data={statuses} colors={colors[2]}/>
          </RowCenter>
        </div>
      );
    } else if (tabName === 'faults') {
      return (
        <div>
          <Row>
            <p>
              Antal insamlingsfel: <Bold>{numberOfFaults}</Bold>,
              varav <Bold>{numberOfUnhandledFaults}</Bold> ohanterade.
            </p>
          </Row>
          <RowCenter>
            <PieChartSelector heading="Städer" data={faultsPerCity} colors={colors[0]}/>
            <PieChartSelector heading="Produktmodeller" data={faultsPerProductModel} colors={colors[1]}/>
          </RowCenter>
        </div>
      );
    }
  })(tabs.graph.selectedOption);

  return (
    <Tabs>
      <TabTopBar>
        <TabHeaders selectedTab={selectedTab} onChangeTab={changeTab}>
          <Tab tab={tabType.graph} title={translate('graph')}/>
          <Tab tab={tabType.list} title={translate('list')}/>
          <Tab tab={tabType.map} title={translate('map')}/>
        </TabHeaders>
        <TabOptions tab={tabType.graph} selectedTab={selectedTab} select={changeTabOption} tabs={tabs}>
          <TabOption title={translate('population')} id="population"/>
          <TabOption title={translate('faults')} id="faults"/>
        </TabOptions>
        <TabSettings useCase={'collection'}/>
      </TabTopBar>
      <TabContent tab={tabType.graph} selectedTab={selectedTab}>
        {graphTabContents}
      </TabContent>
      <TabContent tab={tabType.list} selectedTab={selectedTab}>
        <GatewayList data={{allIds: paginatedList, byId: gateways}}/>
        <PaginationControl pagination={pagination} changePage={paginationChangePage} numOfEntities={numOfGateways}/>
      </TabContent>
      <TabContent tab={tabType.map} selectedTab={selectedTab}>
        <MapContainer markers={gateways} popupMode={PopupMode.gateway}/>
      </TabContent>
    </Tabs>
  );
};

const mapStateToProps = (state: RootState) => {
  const {ui, domainModels} = state;
  const pagination = getCollectionPagination(ui);
  const gateways = domainModels.gateways;
  return {
    selectedTab: getSelectedTab(ui.tabs.collection),
    tabs: getTabs(ui.tabs.collection),
    numOfGateways: getGatewaysTotal(gateways),
    gateways: getGatewayEntities(gateways),
    paginatedList: getPaginationList({...pagination, ...gateways}),
    pagination,
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
  changeTab: changeTabCollection,
  changeTabOption: changeTabOptionCollection,
  paginationChangePage: changePaginationCollection,
}, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(CollectionTabsContainer);

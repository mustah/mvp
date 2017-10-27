import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {Gateway} from '../../../state/domain-models/gateway/gatewayModels';
import {
  getGatewayEntities,
  getGatewaysTotal,
} from '../../../state/domain-models/gateway/gatewaySelectors';
import {changeTab, changeTabOption} from '../../../state/ui/tabs/tabsActions';
import {getSelectedTab, getTabs} from '../../../state/ui/tabs/tabsSelectors';
import {uuid} from '../../../types/Types';
import {ChangePage, PaginationControl} from '../../common/components/pagination-control/PaginationControl';
import {PieChartSelector, PieClick} from '../../common/components/pie-chart-selector/PieChartSelector';
import {Tab} from '../../common/components/tabs/components/Tab';
import {TabContent} from '../../common/components/tabs/components/TabContent';
import {TabHeaders} from '../../common/components/tabs/components/TabHeaders';
import {Tabs} from '../../common/components/tabs/components/Tabs';
import {TabSettings} from '../../common/components/tabs/components/TabSettings';
import {TabTopBar} from '../../common/components/tabs/components/TabTopBar';
import {TabsContainerProps, tabType} from '../../common/components/tabs/models/TabsModel';
import MapContainer from '../../map/containers/MapContainer';
import {GatewayList} from '../components/GatewayList';
import {Pagination} from '../../ui/pagination/paginationModels';
import {paginationChangePage} from '../../ui/pagination/paginationActions';
import {getCollectionPagination, getPaginationList} from '../../ui/pagination/paginationSelectors';

interface CollectionTabsContainer extends TabsContainerProps {
  numOfGateways: number;
  gateways: {[key: string]: Gateway};
  paginatedList: uuid[];
  pagination: Pagination;
  paginationChangePage: (payload: {page: number; useCase: string; }) => any;
}

const CollectionTabsContainer = (props: CollectionTabsContainer) => {
  const {selectedTab, changeTab, gateways, pagination, paginationChangePage, paginatedList, numOfGateways} = props;
  const COLLECTION = 'collection';
  const onChangeTab = (tab: tabType) => {
    changeTab({
      useCase: COLLECTION,
      tab,
    });
  };
  const onChangePagination = (page: number) => {
    paginationChangePage({
      page,
      useCase: COLLECTION,
    });
  };

  const cities = [
    {name: 'Älmhult', value: 822},
    {name: 'Perstorp', value: 893},
  ];

  const selectCity: PieClick = (city: uuid) => alert('You selected the city ' + city);

  const productModels = [
    {name: 'CMe2100', value: 66},
    {name: 'CMi2110', value: 1649},
  ];

  const selectProductModel: PieClick =
    (productModel: uuid) => alert('You selected the product model ' + productModel);

  const statuses = [
    {name: translate('ok'), value: 1713},
    {name: translate('reported'), value: 2},
    {name: translate('unhandled problems'), value: 0},
  ];

  const selectStatus: PieClick =
    (status: uuid) => alert('You selected the status ' + status);

  const colors: [string[]] = [
    ['#e8a090', '#fce8cc'],
    ['#588e95', '#ccd9ce'],
    ['#b7e000', '#f7be29', '#ed4200'],
  ];

  return (
    <Tabs>
      <TabTopBar>
        <TabHeaders selectedTab={selectedTab} onChangeTab={onChangeTab}>
          <Tab tab={tabType.dashboard} title={translate('dashboard')}/>
          <Tab tab={tabType.list} title={translate('list')}/>
          <Tab tab={tabType.map} title={translate('map')}/>
        </TabHeaders>
        <TabSettings useCase={COLLECTION}/>
      </TabTopBar>
      <TabContent tab={tabType.dashboard} selectedTab={selectedTab}>
        <PieChartSelector onClick={selectCity} data={cities} colors={colors[0]}/>
        <PieChartSelector onClick={selectProductModel} data={productModels} colors={colors[1]}/>
        <PieChartSelector onClick={selectStatus} data={statuses} colors={colors[2]}/>
      </TabContent>
      <TabContent tab={tabType.list} selectedTab={selectedTab}>
        <GatewayList data={{allIds: paginatedList, byId: gateways}}/>
        <PaginationControl pagination={pagination} changePage={onChangePagination} numOfEntities={numOfGateways}/>
      </TabContent>
      <TabContent tab={tabType.map} selectedTab={selectedTab}>
        <MapContainer/>
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
  changeTab,
  changeTabOption,
  paginationChangePage,
}, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(CollectionTabsContainer);

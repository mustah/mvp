import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {Gateway} from '../../../state/domain-models/gateway/gatewayModels';
import {getPaginationList} from '../../../state/domain-models/gateway/gatewaySelectors';
import {changeTab, changeTabOption} from '../../../state/ui/tabsActions';
import {uuid} from '../../../types/Types';
import {PaginationControl} from '../../common/components/pagination-control/PaginationControl';
import {Tab} from '../../common/components/tabs/components/Tab';
import {TabContent} from '../../common/components/tabs/components/TabContent';
import {TabHeaders} from '../../common/components/tabs/components/TabHeaders';
import {Tabs} from '../../common/components/tabs/components/Tabs';
import {TabSettings} from '../../common/components/tabs/components/TabSettings';
import {TabTopBar} from '../../common/components/tabs/components/TabTopBar';
import {TabsContainerProps, tabType} from '../../common/components/tabs/models/TabsModel';
import MapContainer from '../../map/containers/MapContainer';
import {collectionChangePage} from '../collectionActions';
import {GatewayList} from '../components/GatewayList';
import {Pagination} from '../models/Collections';

interface CollectionTabsContainer extends TabsContainerProps {
  numOfGateways: number;
  gateways: Gateway;
  paginatedList: uuid[];
  pagination: Pagination;
  collectionChangePage: (page) => any;
}

const CollectionTabsContainer = (props: CollectionTabsContainer) => {
  const {selectedTab, changeTab, gateways, pagination, collectionChangePage, paginatedList, numOfGateways} = props;
  const onChangeTab = (tab: tabType) => {
    changeTab({
      useCase: 'collection',
      tab,
    });
  };

  return (
    <Tabs>
      <TabTopBar>
        <TabHeaders selectedTab={selectedTab} onChangeTab={onChangeTab}>
          <Tab tab={tabType.list} title={translate('list')}/>
          <Tab tab={tabType.map} title={translate('map')}/>
        </TabHeaders>
        <TabSettings useCase="collection"/>
      </TabTopBar>
      <TabContent tab={tabType.list} selectedTab={selectedTab}>
        <GatewayList data={{allIds: paginatedList, byId: gateways}}/>
        <PaginationControl pagination={pagination} changePage={collectionChangePage} nrOfEntities={numOfGateways}/>
      </TabContent>
      <TabContent tab={tabType.map} selectedTab={selectedTab}>
        <MapContainer/>
      </TabContent>
    </Tabs>
  );
};

const mapStateToProps = (state: RootState) => {
  const {ui: {tabs: {collection: {tabs, selectedTab}}}, collection: {pagination}} = state;
  const {domainModels: {gateways: {result, entities, total}}} = state;
  return {
    selectedTab,
    tabs,
    numOfGateways: total,
    gateways: entities.gateways,
    paginatedList: getPaginationList({pagination, gateways: result}),
    pagination,
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
  changeTab,
  changeTabOption,
  collectionChangePage,
}, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(CollectionTabsContainer);

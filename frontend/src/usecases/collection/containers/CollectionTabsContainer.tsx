import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {Dialog} from '../../../components/dialog/Dialog';
import {PaginationControl} from '../../../components/pagination-control/PaginationControl';
import {Tab} from '../../../components/tabs/components/Tab';
import {TabContent} from '../../../components/tabs/components/TabContent';
import {TabHeaders} from '../../../components/tabs/components/TabHeaders';
import {Tabs} from '../../../components/tabs/components/Tabs';
import {TabSettings} from '../../../components/tabs/components/TabSettings';
import {TabTopBar} from '../../../components/tabs/components/TabTopBar';
import {GatewayDetailsContainer} from '../../../containers/dialogs/GatewayDetailsContainer';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {DomainModel} from '../../../state/domain-models/domainModels';
import {getResultDomainModels} from '../../../state/domain-models/domainModelsSelectors';
import {Gateway, GatewayDataSummary} from '../../../state/domain-models/gateway/gatewayModels';
import {
  getGatewayDataSummary,
  getGatewayEntities,
  getGatewaysTotal,
} from '../../../state/domain-models/gateway/gatewaySelectors';
import {addSelection} from '../../../state/search/selection/selectionActions';
import {SelectionParameter} from '../../../state/search/selection/selectionModels';
import {changePaginationCollection} from '../../../state/ui/pagination/paginationActions';
import {OnChangePage, Pagination} from '../../../state/ui/pagination/paginationModels';
import {getCollectionPagination, getPaginationList} from '../../../state/ui/pagination/paginationSelectors';
import {changeTabCollection, changeTabOptionCollection} from '../../../state/ui/tabs/tabsActions';
import {TabName, TabsContainerDispatchToProps, TabsContainerStateToProps} from '../../../state/ui/tabs/tabsModels';
import {getSelectedTab, getTabs} from '../../../state/ui/tabs/tabsSelectors';
import {OnClick, OnClickWithId, uuid} from '../../../types/Types';
import {ClusterContainer} from '../../map/containers/ClusterContainer';
import {Map} from '../../map/containers/Map';
import {closeClusterDialog} from '../../map/mapActions';
import {getSelectedGatewayMarker} from '../../map/mapSelectors';
import {selectEntryAdd} from '../../report/reportActions';
import {CollectionOverview} from '../components/CollectionOverview';
import {GatewayList} from '../components/GatewayList';
import './CollectionTabsContainer.scss';

interface StateToProps extends TabsContainerStateToProps {
  gatewayCount: number;
  gatewaysLookup: DomainModel<Gateway>;
  gatewayDataSummary: GatewayDataSummary | null;
  paginatedList: uuid[];
  pagination: Pagination;
  selectedMaker?: Gateway;
}

interface DispatchToProps extends TabsContainerDispatchToProps {
  paginationChangePage: OnChangePage;
  addSelection: (searchParameters: SelectionParameter) => void;
  selectEntryAdd: OnClickWithId;
  closeClusterDialog: OnClick;
}

const CollectionTabsContainer = (props: StateToProps & DispatchToProps) => {
  const {
    selectedTab,
    changeTab,
    gatewaysLookup,
    gatewayDataSummary,
    pagination,
    paginationChangePage,
    paginatedList,
    gatewayCount,
    addSelection,
    selectEntryAdd,
    selectedMaker,
    closeClusterDialog,
  } = props;

  const dialog = selectedMaker && (
    <Dialog isOpen={true} close={closeClusterDialog}>
      <GatewayDetailsContainer gateway={selectedMaker}/>
    </Dialog>
  );

  return (
    <Tabs>
      <TabTopBar>
        <TabHeaders selectedTab={selectedTab} onChangeTab={changeTab}>
          <Tab tab={TabName.overview} title={translate('overview')}/>
          <Tab tab={TabName.list} title={translate('list')}/>
          <Tab tab={TabName.map} title={translate('map')}/>
        </TabHeaders>
        <TabSettings/>
      </TabTopBar>
      <TabContent tab={TabName.overview} selectedTab={selectedTab}>
        <CollectionOverview gatewayDataSummary={gatewayDataSummary} addSelection={addSelection}/>
      </TabContent>
      <TabContent tab={TabName.list} selectedTab={selectedTab}>
        <GatewayList result={paginatedList} entities={gatewaysLookup} selectEntryAdd={selectEntryAdd}/>
        <PaginationControl pagination={pagination} changePage={paginationChangePage} numOfEntities={gatewayCount}/>
      </TabContent>
      <TabContent tab={TabName.map} selectedTab={selectedTab}>
        <Map>
          <ClusterContainer markers={gatewaysLookup}/>
        </Map>
        {dialog}
      </TabContent>
    </Tabs>
  );
};

const mapStateToProps = ({ui, map, domainModels: {gateways}}: RootState): StateToProps => {
  const pagination = getCollectionPagination(ui);
  return {
    selectedTab: getSelectedTab(ui.tabs.collection),
    tabs: getTabs(ui.tabs.collection),
    gatewayCount: getGatewaysTotal(gateways),
    gatewaysLookup: getGatewayEntities(gateways),
    gatewayDataSummary: getGatewayDataSummary(gateways),
    paginatedList: getPaginationList({pagination, result: getResultDomainModels(gateways)}),
    pagination,
    selectedMaker: getSelectedGatewayMarker(map),
  };
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  changeTab: changeTabCollection,
  changeTabOption: changeTabOptionCollection,
  paginationChangePage: changePaginationCollection,
  addSelection,
  selectEntryAdd,
  closeClusterDialog,
}, dispatch);

export default connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(CollectionTabsContainer);

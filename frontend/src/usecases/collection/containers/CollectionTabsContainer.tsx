import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {Loader} from '../../../components/loading/Loader';
import {PaginationControl} from '../../../components/pagination-control/PaginationControl';
import {Tab} from '../../../components/tabs/components/Tab';
import {TabContent} from '../../../components/tabs/components/TabContent';
import {TabHeaders} from '../../../components/tabs/components/TabHeaders';
import {Tabs} from '../../../components/tabs/components/Tabs';
import {TabSettings} from '../../../components/tabs/components/TabSettings';
import {TabTopBar} from '../../../components/tabs/components/TabTopBar';
import {Maybe} from '../../../helpers/Maybe';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {ObjectsById} from '../../../state/domain-models/domainModels';
import {getResultDomainModels} from '../../../state/domain-models/domainModelsSelectors';
import {Gateway, GatewayDataSummary} from '../../../state/domain-models/gateway/gatewayModels';
import {
  getGatewayDataSummary,
  getGatewayEntities,
  getGatewaysTotal,
} from '../../../state/domain-models/gateway/gatewaySelectors';
import {setSelection} from '../../../state/search/selection/selectionActions';
import {OnSelectParameter} from '../../../state/search/selection/selectionModels';
import {paginationChangePage} from '../../../state/ui/pagination/paginationActions';
import {OnChangePage, Pagination} from '../../../state/ui/pagination/paginationModels';
import {getPagination, getPaginationList} from '../../../state/ui/pagination/paginationSelectors';
import {changeTabCollection} from '../../../state/ui/tabs/tabsActions';
import {TabName, TabsContainerDispatchToProps, TabsContainerStateToProps} from '../../../state/ui/tabs/tabsModels';
import {getSelectedTab} from '../../../state/ui/tabs/tabsSelectors';
import {OnClick, OnClickWithId, uuid} from '../../../types/Types';
import {closeClusterDialog} from '../../map/mapActions';
import {getSelectedGatewayMarker} from '../../map/mapSelectors';
import {selectEntryAdd} from '../../report/reportActions';
import {GatewayList} from '../components/GatewayList';

interface StateToProps extends TabsContainerStateToProps {
  gatewayCount: number;
  gateways: ObjectsById<Gateway>;
  gatewayDataSummary: Maybe<GatewayDataSummary>;
  paginatedList: uuid[];
  pagination: Pagination;
  selectedMaker: Maybe<Gateway>;
  isFetching: boolean;
}

interface DispatchToProps extends TabsContainerDispatchToProps {
  paginationChangePage: OnChangePage;
  setSelection: OnSelectParameter;
  selectEntryAdd: OnClickWithId;
  closeClusterDialog: OnClick;
}

const componentId = 'gatewayList';

const CollectionTabsContainer = (props: StateToProps & DispatchToProps) => {
  const {
    selectedTab,
    changeTab,
    gateways,
    // gatewayDataSummary,
    pagination,
    paginationChangePage,
    paginatedList,
    gatewayCount,
    // setSelection,
    selectEntryAdd,
    // selectedMaker,
    // closeClusterDialog,
    isFetching,
  } = props;

  // const hasGateways: boolean = isMarkersWithinThreshold(gateways);

  // const dialog = selectedMaker.isJust() && (
  //   <Dialog isOpen={true} close={closeClusterDialog}>
  //     <GatewayDetailsContainer gateway={selectedMaker.get()}/>
  //   </Dialog>
  // );

  const changePage = (page: number) => (paginationChangePage({
    model: 'gateways',
    componentId,
    page,
  }));

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
        {/*<Loader isFetching={isFetching}>*/}
          {/*<CollectionOverview gatewayDataSummary={gatewayDataSummary} setSelection={setSelection}/>*/}
        {/*</Loader>*/}
      </TabContent>
      <TabContent tab={TabName.list} selectedTab={selectedTab}>
        <Loader isFetching={isFetching}>
          <div>
            <GatewayList result={paginatedList} entities={gateways} selectEntryAdd={selectEntryAdd}/>
            <PaginationControl pagination={{...pagination, totalElements: gatewayCount}} changePage={changePage} />
          </div>
        </Loader>
      </TabContent>
      <TabContent tab={TabName.map} selectedTab={selectedTab}>
        {/*<Loader isFetching={isFetching}>*/}
          {/*<div>*/}
            {/*<Content hasContent={hasGateways} noContentText={translate('no gateways')}>*/}
              {/*<Map>*/}
                {/*<ClusterContainer markers={gateways}/>*/}
              {/*</Map>*/}
            {/*</Content>*/}
            {/*{dialog}*/}
          {/*</div>*/}
        {/*</Loader>*/}
      </TabContent>
    </Tabs>
  );
};

const mapStateToProps = ({ui: {pagination, tabs}, map, domainModels: {gateways}}: RootState): StateToProps => {
  const paginationData: Pagination = getPagination({pagination, componentId: 'gatewayList', model: 'gateways'});
  return {
    selectedTab: getSelectedTab(tabs.collection),
    gatewayCount: getGatewaysTotal(gateways),
    gateways: getGatewayEntities(gateways),
    gatewayDataSummary: getGatewayDataSummary(gateways),
    paginatedList: getPaginationList({
      page: paginationData.page,
      size: paginationData.size,
      result: getResultDomainModels(gateways),
    }),
    pagination: paginationData,
    selectedMaker: getSelectedGatewayMarker(map),
    isFetching: gateways.isFetching,
  };
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  changeTab: changeTabCollection,
  paginationChangePage,
  setSelection,
  selectEntryAdd,
  closeClusterDialog,
}, dispatch);

export default connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(CollectionTabsContainer);

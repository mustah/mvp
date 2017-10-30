import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {Meter} from '../../../state/domain-models/meter/meterModels';
import {getMeterEntities, getMetersTotal} from '../../../state/domain-models/meter/meterSelectors';
import {changeTab, changeTabOption} from '../../../state/ui/tabs/tabsActions';
import {getSelectedTab, getTabs} from '../../../state/ui/tabs/tabsSelectors';
import {uuid} from '../../../types/Types';
import {PaginationControl} from '../../common/components/pagination-control/PaginationControl';
import {MeterList} from '../../common/components/table/MeterList';
import {Tab} from '../../common/components/tabs/components/Tab';
import {TabContent} from '../../common/components/tabs/components/TabContent';
import {TabHeaders} from '../../common/components/tabs/components/TabHeaders';
import {Tabs} from '../../common/components/tabs/components/Tabs';
import {TabSettings} from '../../common/components/tabs/components/TabSettings';
import {TabTopBar} from '../../common/components/tabs/components/TabTopBar';
import {TabsContainerProps, tabType} from '../../common/components/tabs/models/TabsModel';
import MapContainer from '../../map/containers/MapContainer';
import {paginationChangePage} from '../../ui/pagination/paginationActions';
import {Pagination} from '../../ui/pagination/paginationModels';
import {getDashboardPagination, getPaginationList} from '../../ui/pagination/paginationSelectors';

interface DashboardTabsContainerProps extends TabsContainerProps {
  numOfMeters: number;
  meters: {[key: string]: Meter};
  paginatedList: uuid[];
  pagination: Pagination;
  paginationChangePage: (payload: {page: number; useCase: string; }) => any;
}

const DashboardTabsContainer = (props: DashboardTabsContainerProps) => {
  const {
    selectedTab, changeTab,
    meters, pagination, paginationChangePage, paginatedList, numOfMeters,
  } = props;

  const DASHBOARD = 'dashboard';
  const onChangeTab = (tab: tabType) => {
    changeTab({
      useCase: 'dashboard',
      tab,
    });
  };
  const onChangePagination = (page: number) => {
    paginationChangePage({
      page,
      useCase: DASHBOARD,
    });
  };

  return (
    <Tabs>
      <TabTopBar>
        <TabHeaders selectedTab={selectedTab} onChangeTab={onChangeTab}>
          <Tab tab={tabType.list} title={translate('list')}/>
          <Tab tab={tabType.map} title={translate('map')}/>
        </TabHeaders>
        <TabSettings useCase="dashboard"/>
      </TabTopBar>
      <TabContent tab={tabType.list} selectedTab={selectedTab}>
        <MeterList data={{allIds: paginatedList, byId: meters}}/>
        <PaginationControl pagination={pagination} numOfEntities={numOfMeters} changePage={onChangePagination}/>
      </TabContent>
      <TabContent tab={tabType.map} selectedTab={selectedTab}>
        <MapContainer/>
      </TabContent>
    </Tabs>
  );
};

const mapStateToProps = (state: RootState) => {
  const {ui, domainModels} = state;
  const pagination = getDashboardPagination(ui);
  const meters = domainModels.meters;
  return {
    selectedTab: getSelectedTab(ui.tabs.dashboard),
    tabs: getTabs(ui.tabs.dashboard),
    numOfMeters: getMetersTotal(meters),
    meters: getMeterEntities(meters),
    paginatedList: getPaginationList({...pagination, ...meters}),
    pagination,
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
  changeTab,
  changeTabOption,
  paginationChangePage,
}, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(DashboardTabsContainer);

import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {Meter} from '../../../state/domain-models/meter/meterModels';
import {getMeterEntities, getMetersTotal} from '../../../state/domain-models/meter/meterSelectors';
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
import {Pagination} from '../../../state/ui/pagination/paginationModels';
import {getDashboardPagination, getPaginationList} from '../../../state/ui/pagination/paginationSelectors';
import {changeTabDashboard, changeTabOptionDashboard} from '../../../state/ui/tabs/tabsActions';
import {changePaginationDashboard} from '../../../state/ui/pagination/paginationActions';

interface DashboardTabsContainerProps extends TabsContainerProps {
  numOfMeters: number;
  meters: {[key: string]: Meter};
  paginatedList: uuid[];
  pagination: Pagination;
  paginationChangePage: (page: number) => any;
}

const DashboardTabsContainer = (props: DashboardTabsContainerProps) => {
  const {
    selectedTab, changeTab,
    meters, pagination, paginationChangePage, paginatedList, numOfMeters,
  } = props;

  return (
    <Tabs>
      <TabTopBar>
        <TabHeaders selectedTab={selectedTab} onChangeTab={changeTab}>
          <Tab tab={tabType.list} title={translate('list')}/>
          <Tab tab={tabType.map} title={translate('map')}/>
        </TabHeaders>
        <TabSettings useCase="dashboard"/>
      </TabTopBar>
      <TabContent tab={tabType.list} selectedTab={selectedTab}>
        <MeterList data={{allIds: paginatedList, byId: meters}}/>
        <PaginationControl pagination={pagination} numOfEntities={numOfMeters} changePage={paginationChangePage}/>
      </TabContent>
      <TabContent tab={tabType.map} selectedTab={selectedTab}>
        <MapContainer markers={meters}/>
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
  changeTab: changeTabDashboard,
  changeTabOption: changeTabOptionDashboard,
  paginationChangePage: changePaginationDashboard,
}, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(DashboardTabsContainer);

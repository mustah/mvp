import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {MeteringPoint} from '../../common/components/table/meteringPoint/MeteringPoint';
import {Status} from '../../common/components/table/status/Status';
import {Table} from '../../common/components/table/table/Table';
import {TableHead} from '../../common/components/table/table/TableHead';
import {TableColumn} from '../../common/components/table/tableColumn/TableColumn';
import {Tab} from '../../common/components/tabs/components/Tab';
import {TabContent} from '../../common/components/tabs/components/TabContent';
import {TabHeaders} from '../../common/components/tabs/components/TabHeaders';
import {Tabs} from '../../common/components/tabs/components/Tabs';
import {TabSettings} from '../../common/components/tabs/components/TabSettings';
import {TabTopBar} from '../../common/components/tabs/components/TabTopBar';
import {TabsContainerProps, tabType} from '../../common/components/tabs/models/TabsModel';
import {changeTab, changeTabOption} from '../../../state/ui/tabsActions';
import {normalizedData} from '../models/dashboardModels';
import MapContainer from '../components/map/containers/MapContainer';

const DashboardTabsContainer = (props: TabsContainerProps) => {
  const {selectedTab, changeTab} = props;
  const onChangeTab = (tab: tabType) => {
    changeTab({
      useCase: 'dashboard',
      tab,
    });
  };
  const renderMeteringPointCell = (value, index) => <MeteringPoint id={value}/>;
  const renderStatusCell = (value, index) => <Status code={value.code} content={value.text}/>;

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
        <Table data={normalizedData.meteringPoints}>
          <TableColumn
            id={'id'}
            header={<TableHead>{translate('meter')}</TableHead>}
            cell={renderMeteringPointCell}
          />
          <TableColumn
            id={'type'}
            header={<TableHead>{translate('type')}</TableHead>}
          />
          <TableColumn
            id={'location'}
            header={<TableHead>{translate('location')}</TableHead>}
          />
          <TableColumn
            id={'gateway'}
            header={<TableHead>{translate('gateway')}</TableHead>}
          />
          <TableColumn
            id={'status'}
            header={<TableHead sortable={true} currentSort={'asc'}>{translate('status')}</TableHead>}
            cell={renderStatusCell}
          />
        </Table>
      </TabContent>
      <TabContent tab={tabType.map} selectedTab={selectedTab}>
        <MapContainer/>
      </TabContent>
    </Tabs>
  );
};

const mapStateToProps = (state: RootState) => {
  const {ui: {tabs: {dashboard: {tabs, selectedTab}}}} = state;
  return {
    selectedTab,
    tabs,
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
  changeTab,
  changeTabOption,
}, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(DashboardTabsContainer);

import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/index';
import {translate} from '../../../services/translationService';
import {MeteringPoint} from '../../common/components/table/meteringPoint/MeteringPoint';
import {Status} from '../../common/components/table/status/Status';
import {Table} from '../../common/components/table/table/Table';
import {TableHead} from '../../common/components/table/table/TableHead';
import {TableColumn} from '../../common/components/table/tableColumn/TableColumn';
import {Tab} from '../../common/components/tabs/components/Tab';
import {TabContent} from '../../common/components/tabs/components/TabContent';
import {TabHeaders} from '../../common/components/tabs/components/TabHeaders';
import {TabOption} from '../../common/components/tabs/components/TabOption';
import {TabOptions} from '../../common/components/tabs/components/TabOptions';
import {Tabs} from '../../common/components/tabs/components/Tabs';
import {TabSettings} from '../../common/components/tabs/components/TabSettings';
import {TabTopBar} from '../../common/components/tabs/components/TabTopBar';
import {TabsContainerProps, tabType} from '../../common/components/tabs/models/TabsModel';
import {changeTab, changeTabOption} from '../../ui/tabsActions';
import {normalizedData} from '../models/dashboardModels';
import {MoidMap} from '../components/map/MoidMap';

const DashboardTabsContainer = (props: TabsContainerProps) => {
  const {tabs, selectedTab, changeTab, changeTabOption} = props;
  const onChangeTab = (tab: tabType) => {
    changeTab({
      useCase: 'dashboard',
      tab,
    });
  };
  const onChangeTabOption = (tab: tabType, option: string): void => {
    changeTabOption({
      useCase: 'dashboard',
      tab,
      option,
    });
  };
  const renderMeteringPointCell = (value, index) => <MeteringPoint id={value}/>;
  const renderStatusCell = (value, index) => <Status code={value.code} content={value.text}/>;

  return (
    <Tabs>
      <TabTopBar>
        <TabHeaders selectedTab={selectedTab} onChangeTab={onChangeTab}>
          <Tab tab={tabType.list} title={translate('list')} />
          <Tab tab={tabType.map} title={translate('map')} />
        </TabHeaders>
        <TabOptions tab={tabType.map} selectedTab={selectedTab} select={onChangeTabOption} tabs={tabs}>
          <TabOption
            title={translate('area')}
            id={'area'}
          />
          <TabOption
            title={translate('object')}
            id={'object'}
          />
          <TabOption
            title={translate('facility')}
            id={'facility'}
          />
        </TabOptions>
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
        <MoidMap/>
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
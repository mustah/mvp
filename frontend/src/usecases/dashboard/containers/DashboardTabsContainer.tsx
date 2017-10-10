import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/index';
import {translate} from '../../../services/translationService';
import {Image} from '../../common/components/images/Image';
import {MeteringPoint} from '../../table/components/meteringPoint/MeteringPoint';
import {StatusIcon} from '../../table/components/statusIcon/StatusIcon';
import {Table} from '../../table/components/table/Table';
import {TableHead} from '../../table/components/table/TableHead';
import {TableColumn} from '../../table/components/tableColumn/TableColumn';
import {Tab} from '../../tabs/components/Tab';
import {TabContent} from '../../tabs/components/TabContent';
import {TabList} from '../../tabs/components/TabList';
import {TabOption} from '../../tabs/components/TabOption';
import {TabOptions} from '../../tabs/components/TabOptions';
import {Tabs} from '../../tabs/components/Tabs';
import {TabSettings} from '../../tabs/components/TabSettings';
import {TabsContainerProps, tabType} from '../../tabs/models/TabsModel';
import {changeTab, changeTabOption} from '../../tabs/tabsActions';
import {normalizedData} from '../models/dashboardModels';

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
  const renderStatusCell = (value, index) => <StatusIcon code={value.code} content={value.text}/>;

  return (
    <Tabs>
      <TabList>
        <Tab tab={tabType.list} title={translate('list')} selectedTab={selectedTab} onChangeTab={onChangeTab}/>
        <Tab tab={tabType.map} title={translate('map')} selectedTab={selectedTab} onChangeTab={onChangeTab}/>
        <TabOptions forTab={tabType.map} selectedTab={selectedTab} >
          <TabOption
            tab={tabType.map}
            select={onChangeTabOption}
            title={translate('area')}
            id={'area'}
            selectedOption={tabs[tabType.map].selectedOption}
          />
          <TabOption
            tab={tabType.map}
            select={onChangeTabOption}
            title={translate('object')}
            id={'object'}
            selectedOption={tabs[tabType.map].selectedOption}
          />
          <TabOption
            tab={tabType.map}
            select={onChangeTabOption}
            title={translate('facility')}
            id={'facility'}
            selectedOption={tabs[tabType.map].selectedOption}
          />
        </TabOptions>
        <TabSettings useCase="dashboard"/>
      </TabList>
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
        <Image src="usecases/validation/img/map.png"/>
      </TabContent>
    </Tabs>
  );
};
const mapStateToProps = (state: RootState) => {
  const {tabs: {dashboard: {tabs, selectedTab}}} = state;
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

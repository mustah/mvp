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
import {TabsContainerProps, tabTypes} from '../../tabs/models/TabsModel';
import {changeTab, changeTabOption} from '../../tabs/tabsActions';
import {normalizedData} from '../models/dashboardModels';

const DashboardTabsContainer = (props: TabsContainerProps) => {
  const {tabs, selectedTab, changeTab, changeTabOption} = props;
  const onChangeTab = (tab: string) => {
    changeTab({
      useCase: 'dashboard',
      tab,
    });
  };
  const onChangeTabOption = (tab: string, option: string): void => {
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
        <Tab tab={tabTypes.list} title={translate('list')} selectedTab={selectedTab} onChangeTab={onChangeTab}/>
        <Tab tab={tabTypes.map} title={translate('map')} selectedTab={selectedTab} onChangeTab={onChangeTab}/>
        <TabOptions forTab={tabTypes.map} selectedTab={selectedTab} >
          <TabOption
            tab={tabTypes.map}
            select={onChangeTabOption}
            optionName={translate('area')}
            option={'area'}
            selectedOption={tabs[tabTypes.map].selectedOption}
          />
          <TabOption
            tab={tabTypes.map}
            select={onChangeTabOption}
            optionName={translate('object')}
            option={'object'}
            selectedOption={tabs[tabTypes.map].selectedOption}
          />
          <TabOption
            tab={tabTypes.map}
            select={onChangeTabOption}
            optionName={translate('facility')}
            option={'facility'}
            selectedOption={tabs[tabTypes.map].selectedOption}
          />
        </TabOptions>
        <TabSettings useCase="dashboard"/>
      </TabList>
      <TabContent tab={tabTypes.list} selectedTab={selectedTab}>
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
      <TabContent tab={tabTypes.map} selectedTab={selectedTab}>
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

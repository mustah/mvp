import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/index';
import {translate} from '../../../services/translationService';
import {Image} from '../../common/components/images/Image';
import {Tab} from '../../tabs/components/Tab';
import {TabContent} from '../../tabs/components/TabContent';
import {TabList} from '../../tabs/components/TabList';
import {TabOption} from '../../tabs/components/TabOption';
import {TabOptions} from '../../tabs/components/TabOptions';
import {Tabs} from '../../tabs/components/Tabs';
import {TabSettings} from '../../tabs/components/TabSettings';
import {TabsContainerProps, tabType} from '../../tabs/models/TabsModel';
import {changeTab, changeTabOption} from '../../tabs/tabsActions';

const ValidationTabsContainer = (props: TabsContainerProps) => {
  const {tabs, selectedTab, changeTab, changeTabOption} = props;
  const onChangeTab = (tab: tabType) => {
    changeTab({
      useCase: 'validation',
      tab,
    });
  };
  const onChangeTabOption = (tab: tabType, option: string): void => {
    changeTabOption({
      useCase: 'validation',
      tab,
      option,
    });
  };

  return (
    <Tabs>
      <TabList>
        <Tab title={translate('map')} tab={tabType.map} selectedTab={selectedTab} onChangeTab={onChangeTab}/>
        <Tab title={translate('list')} tab={tabType.list} selectedTab={selectedTab} onChangeTab={onChangeTab}/>
        <TabOptions forTab={tabType.map} selectedTab={selectedTab}>
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
        <TabSettings useCase="validation"/>
      </TabList>
      <TabContent tab={tabType.map} selectedTab={selectedTab}>
        <Image src="usecases/validation/img/map.png"/>
      </TabContent>
      <TabContent tab={tabType.list} selectedTab={selectedTab}>
        <Image src="usecases/validation/img/meters.png"/>
      </TabContent>
    </Tabs>
  );
};

const mapStateToProps = (state: RootState) => {
  const {tabs: {validation: {tabs, selectedTab}}} = state;
  return {
    selectedTab,
    tabs,
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
  changeTab,
  changeTabOption,
}, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(ValidationTabsContainer);

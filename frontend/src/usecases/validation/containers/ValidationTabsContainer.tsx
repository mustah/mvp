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
import {Tabs} from '../../tabs/components/Tabs';
import {TabSettings} from '../../tabs/components/TabSettings';
import {TabsContainerProps, tabTypes} from '../../tabs/models/TabsModel';
import {changeTab, changeTabOption} from '../../tabs/tabsActions';
import {TabOptions} from '../../tabs/components/TabOptions';

const ValidationTabsContainer = (props: TabsContainerProps) => {
  const {tabs, selectedTab, changeTab, changeTabOption} = props;
  const onChangeTab = (tab: string) => {
    changeTab({
      useCase: 'validation',
      tab,
    });
  };
  const onChangeTabOption = (tab: string, option: string): void => {
    changeTabOption({
      useCase: 'validation',
      tab,
      option,
    });
  };

  return (
    <Tabs>
      <TabList>
        <Tab title={translate('map')} tab={tabTypes.map} selectedTab={selectedTab} onChangeTab={onChangeTab}/>
        <Tab title={translate('list')} tab={tabTypes.list} selectedTab={selectedTab} onChangeTab={onChangeTab}/>
        <TabOptions forTab={tabTypes.map} selectedTab={selectedTab}>
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
        <TabOptions forTab={tabTypes.list} selectedTab={selectedTab}>
          <TabOption
            tab={tabTypes.list}
            select={onChangeTabOption}
            optionName={translate('sort descending')}
            option={'sort descending'}
            selectedOption={tabs[tabTypes.list].selectedOption}
          />
          <TabOption
            tab={tabTypes.list}
            select={onChangeTabOption}
            optionName={translate('sort ascending')}
            option={'sort ascending'}
            selectedOption={tabs[tabTypes.list].selectedOption}
          />
        </TabOptions>
        <TabSettings/>
      </TabList>
      <TabContent tab={tabTypes.map} selectedTab={selectedTab}>
        <Image src="usecases/validation/img/map.png" />
      </TabContent>
      <TabContent tab={tabTypes.list} selectedTab={selectedTab}>
        <Image src="usecases/validation/img/meters.png" />
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

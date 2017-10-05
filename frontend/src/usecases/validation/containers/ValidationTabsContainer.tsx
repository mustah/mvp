import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/index';
import {translate} from '../../../services/translationService';
import {TabItem} from '../../tabs/components/TabItem';
import {TabOption, TabOptionProps} from '../../tabs/components/TabOption';
import {Tabs} from '../../tabs/components/Tabs';
import {Tab, tabTypes} from '../../tabs/models/TabsModel';
import {changeTab, changeTabOption} from '../../tabs/tabsActions';

interface ValidationTabsContainerProps {
  tabs: Tab;
  selectedTab: string;
  changeTab: (payload) => any;
  changeTabOption: (payload) => any;
}

const ValidationTabsContainer = (props: ValidationTabsContainerProps) => {
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
    <Tabs selectedTab={selectedTab}>
      <TabItem tabName={translate('map')} tab={tabTypes.map} selectedTab={selectedTab} changeTab={onChangeTab}>
        <TabContent content={'hej hej'}>
          <TabOption
            tab={tabTypes.map}
            tabOptionAction={onChangeTabOption}
            optionName={translate('option a')}
            option={'option a'}
            selectedOption={tabs[tabTypes.map].selectedOption}
          />
          <TabOption
            tab={tabTypes.map}
            tabOptionAction={onChangeTabOption}
            optionName={translate('option b')}
            option={'option b'}
            selectedOption={tabs[tabTypes.map].selectedOption}
          />
        </TabContent>
      </TabItem>
      <TabItem tabName={translate('list')} tab={tabTypes.list} selectedTab={selectedTab} changeTab={onChangeTab}>
        <TabContent content={'Another content'}>
          <TabOption
            tab={tabTypes.list}
            tabOptionAction={onChangeTabOption}
            optionName={translate('option 1')}
            option={'option 1'}
            selectedOption={tabs[tabTypes.list].selectedOption}
          />
          <TabOption
            tab={tabTypes.list}
            tabOptionAction={onChangeTabOption}
            optionName={translate('option 2')}
            option={'option 2'}
            selectedOption={tabs[tabTypes.list].selectedOption}
          />
        </TabContent>
      </TabItem>
    </Tabs>
  );
};

export interface TabContentProps {
  content: string;
  children: Array<React.ReactElement<TabOptionProps>>;
  // TODO: Perhaps create a common type for typing children without using so not have to write out Array<> and
  // React.ReactElement all the time.
}

const TabContent = (props: TabContentProps) => {
  return (
    <div>
      {props.content}
    </div>);
};

const mapStateToProps = (state: RootState) => {
  const {tabs} = state;
  return {
    selectedTab: tabs.validation.selectedTab,
    tabs: tabs.validation.tabs,
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
  changeTab,
  changeTabOption,
}, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(ValidationTabsContainer);

import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/index';
import {TabItem} from '../../tabs/components/tabItem/TabItem';
import {TabOption} from '../../tabs/components/tabOption/TabOption';
import {TabsContainer} from '../../tabs/containers/TabsContainer';
import {Tabs, TabTypes} from '../../tabs/models/Tabs';
import {changeTab, changeTabOption} from '../../tabs/tabsActions';

interface ValidationTabsContainerProps {
  tabs: Tabs;
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
    <TabsContainer selectedTab={selectedTab}>
      <TabItem tabName={TabTypes.map} isSelected={selectedTab === TabTypes.map} changeTab={onChangeTab}>
        <TabContent content={'hej hej'}>
          <TabOption
            tab={TabTypes.map}
            tabOptionAction={onChangeTabOption}
            option={'OptionA'}
            isSelected={tabs[TabTypes.map].selectedOption === 'OptionA'}
          />
          <TabOption
            tab={TabTypes.map}
            tabOptionAction={onChangeTabOption}
            option={'OptionB'}
            isSelected={tabs[TabTypes.map].selectedOption === 'OptionB'}
          />
        </TabContent>
      </TabItem>
      <TabItem tabName={TabTypes.list} isSelected={selectedTab === TabTypes.list} changeTab={onChangeTab}>
        <TabContent content={'Another content'}>
          <TabOption
            tab={TabTypes.list}
            tabOptionAction={onChangeTabOption}
            option={'Option1'}
            isSelected={tabs[TabTypes.list].selectedOption === 'Option1'}
          />
          <TabOption
            tab={TabTypes.list}
            tabOptionAction={onChangeTabOption}
            option={'Option2'}
            isSelected={tabs[TabTypes.list].selectedOption === 'Option2'}
          />
        </TabContent>
      </TabItem>
    </TabsContainer>
  );
};

const TabContent = (props: {content: string}) => {
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

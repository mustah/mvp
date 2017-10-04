import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history4/redirect';
import {RootState} from '../../../reducers/index';
import {SelectionOverview} from '../../common/components/selectionoverview/SelectionOverview';
import {Column} from '../../layouts/components/column/Column';
import {Content} from '../../layouts/components/content/Content';
import {Layout} from '../../layouts/components/layout/Layout';
import {TabItem} from '../../tabs/components/tabItem/TabItem';
import {TabOption} from '../../tabs/components/tabOption/TabOption';
import {TabsContainer} from '../../tabs/containers/TabsContainer';
import {Tabs, TabTypes} from '../../tabs/models/Tabs';
import {changeTab, changeTabOption} from '../../tabs/tabsActions';
import {ValidationState} from '../models/Validations';
import {fetchValidations} from '../validationActions';
import {ValidationOverviewContainer} from './ValidationOverviewContainer';

export interface ValidationContainerProps {
  fetchValidations: () => any;
  validation: ValidationState;
  selectedTab: string;
  tabs: Tabs;
  changeTab: (payload) => any;
  changeTabOption: (payload) => any;
}

const ValidationContainer = (props: ValidationContainerProps & InjectedAuthRouterProps) => {
  const {fetchValidations, changeTab, changeTabOption, selectedTab, tabs} = props;
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
    <Layout>
      <Column className="flex-1">
        <SelectionOverview title={'Allt'}/>
        <Content>
          <ValidationOverviewContainer/>
          <div className="button" onClick={fetchValidations}>VALIDATIONS</div>

          <TabsContainer selectedTab={selectedTab}>
            <TabItem tabName={TabTypes.map} isSelected={selectedTab === TabTypes.map} changeTab={onChangeTab}>
              <TabContent content={'hej hej'}>
                <TabOption
                  tab={TabTypes.map}
                  tabOptionAction={onChangeTabOption}
                  option={'OptionA'}
                  key={1}
                  isSelected={tabs[TabTypes.map].selectedOption === 'OptionA'}
                />
                <TabOption
                  tab={TabTypes.map}
                  tabOptionAction={onChangeTabOption}
                  option={'OptionB'}
                  key={2}
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
                  key={1}
                  isSelected={tabs[TabTypes.list].selectedOption === 'Option1'}
                />
                <TabOption
                  tab={TabTypes.list}
                  tabOptionAction={onChangeTabOption}
                  option={'Option2'}
                  key={2}
                  isSelected={tabs[TabTypes.list].selectedOption === 'Option2'}
                />
              </TabContent>
            </TabItem>
          </TabsContainer>

        </Content>
      </Column>
    </Layout>
  );
};

const TabContent = (props: {content: string}) => {
  return (
    <div>
      {props.content}
    </div>);
};

const mapStateToProps = (state: RootState) => {
  const {validation, tabs} = state;
  return {
    validation,
    selectedTab: tabs.validation.selectedTab,
    tabs: tabs.validation.tabs,
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
  fetchValidations,
  changeTab,
  changeTabOption,
}, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(ValidationContainer);

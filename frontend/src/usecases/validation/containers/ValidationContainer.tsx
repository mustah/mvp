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
import {TabOption} from '../../tabs/components/tabOptions/TabOption';
import {TabsContainer} from '../../tabs/containers/TabsContainer';
import {TabTypes, TabView} from '../../tabs/models/Tabs';
import {changeTab, changeTabOption} from '../../tabs/tabsActions';
import {ValidationState} from '../models/Validations';
import {fetchValidations} from '../validationActions';
import {ValidationOverviewContainer} from './ValidationOverviewContainer';

export interface ValidationContainerProps {
  fetchValidations: () => any;
  validation: ValidationState;
  tabView: TabView;
  changeTab: (payload) => any;
  changeTabOption: (payload) => any;
}

const ValidationContainer = (props: ValidationContainerProps & InjectedAuthRouterProps) => {
  const {fetchValidations, tabView, changeTab, changeTabOption} = props;
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

          <TabsContainer tabView={tabView}>
            <TabItem tabName={TabTypes.map} isSelected={tabView.selectedTab === TabTypes.map} changeTab={onChangeTab}>
              <TmpComponent content={'hej hej'}>
                <TabOption
                  tab={TabTypes.map}
                  tabOptionAction={onChangeTabOption}
                  option={'OptionA'}
                  key={1}
                  isSelected={tabView.tabs[TabTypes.map].selectedOption === 'OptionA'}
                />
                <TabOption
                  tab={TabTypes.map}
                  tabOptionAction={onChangeTabOption}
                  option={'OptionB'}
                  key={2}
                  isSelected={tabView.tabs[TabTypes.map].selectedOption === 'OptionB'}
                />
              </TmpComponent>
            </TabItem>
            <TabItem tabName={TabTypes.list} isSelected={tabView.selectedTab === TabTypes.list} changeTab={onChangeTab}>
              <TmpComponent content={'Another content'}>
                <TabOption
                  tab={TabTypes.list}
                  tabOptionAction={onChangeTabOption}
                  option={'Option1'}
                  key={1}
                  isSelected={tabView.tabs[TabTypes.list].selectedOption === 'Option1'}
                />
                <TabOption
                  tab={TabTypes.list}
                  tabOptionAction={onChangeTabOption}
                  option={'Option2'}
                  key={2}
                  isSelected={tabView.tabs[TabTypes.list].selectedOption === 'Option2'}
                />
              </TmpComponent>
            </TabItem>
          </TabsContainer>

        </Content>
      </Column>
    </Layout>
  );
};

const TmpComponent = (props: {content: string}) => {
  return (
    <div>
      {props.content}
    </div>);
};

const mapStateToProps = (state: RootState) => {
  const {validation, tabs} = state;
  return {
    validation,
    tabView: tabs.validation,
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
  fetchValidations,
  changeTab,
  changeTabOption,
}, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(ValidationContainer);

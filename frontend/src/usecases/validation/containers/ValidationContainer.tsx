import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history4/redirect';
import {RootState} from '../../../reducers/index';
import {SelectionOverview} from '../../common/components/selectionoverview/SelectionOverview';
import {Column} from '../../layouts/components/column/Column';
import {Content} from '../../layouts/components/content/Content';
import {Layout} from '../../layouts/components/layout/Layout';
import {TabItem} from '../../viewSwitch/components/tabItem/TabItem';
import {ViewSwitchContainer} from '../../viewSwitch/containers/ViewSwitchContainer';
import {TabTypes} from '../../viewSwitch/models/Tabs';
import {viewSwitchChangeTab} from '../../viewSwitch/viewSwitchActions';
import {TabView} from '../../viewSwitch/viewSwitchReducer';
import {ValidationState} from '../models/Validations';
import {fetchValidations} from '../validationActions';
import {ValidationOverviewContainer} from './ValidationOverviewContainer';

export interface ValidationContainerProps {
  fetchValidations: () => any;
  validation: ValidationState;
  tabView: TabView;
  viewSwitchChangeTab: (payload) => any;
}

const ValidationContainer = (props: ValidationContainerProps & InjectedAuthRouterProps) => {
  const {fetchValidations, tabView, viewSwitchChangeTab} = props;
  const changeTab = (tab: string) => {
    viewSwitchChangeTab({
      useCase: 'validation',
      tab,
    });
  };
  return (
    <Layout>
      <Column className="flex-1">
        <SelectionOverview title={'Allt'}/>
        <Content>
          <ValidationOverviewContainer/>
          <div className="button" onClick={fetchValidations}>VALIDATIONS</div>

          <ViewSwitchContainer tabView={tabView}>
            <TabItem tabName={TabTypes.map} isSelected={tabView.selectedTab === TabTypes.map} changeTab={changeTab}>
              <TmpComponent options={['Test1', 'Test2']}/>
            </TabItem>
            <TabItem tabName={TabTypes.list} isSelected={tabView.selectedTab === TabTypes.list} changeTab={changeTab}>
              <div>Tab content 2</div>
            </TabItem>
          </ViewSwitchContainer>

        </Content>
      </Column>
    </Layout>
  );
};

const TmpComponent = (props) => {
  return (
    <div>
      Hej hej
    </div>);
};

const mapStateToProps = (state: RootState) => {
  const {validation, viewSwitch} = state;
  return {
    validation,
    tabView: viewSwitch.validation,
  }
    ;
};

const mapDispatchToProps = dispatch => bindActionCreators({
  fetchValidations,
  viewSwitchChangeTab,
}, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(ValidationContainer);

import * as React from 'react';
import {Row} from '../../layouts/components/row/Row';
import {TabItem} from '../components/tabItem/TabItem';
import {TabModes} from '../components/tabModes/TabModes';
import {TabOptions} from '../components/tabOptions/TabOptions';
import {TabView} from '../viewSwitchReducer';

interface ViewSwitchContainerProps {
  useCase: string;
  viewSwitchChangeTab: (payload) => any;
  tabView: TabView;
}

export const ViewSwitchContainer = (props: ViewSwitchContainerProps) => {
  const {useCase, viewSwitchChangeTab, tabView} = props;

  const changeTab = (tab: string) => {
    viewSwitchChangeTab({
      useCase,
      tab,
    });
  };

  return (
    <Row>
      <TabItem tabName={'map'} isSelected={tabView.selectedTab === 'map'} changeTab={changeTab}/>
      <TabItem tabName={'list'} isSelected={tabView.selectedTab === 'list'} changeTab={changeTab}/>
      <TabModes/>
      <TabOptions/>
    </Row>
  );
};

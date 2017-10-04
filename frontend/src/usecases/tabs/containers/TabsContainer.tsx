import * as React from 'react';
import {Column} from '../../layouts/components/column/Column';
import {Row} from '../../layouts/components/row/Row';
import {TabOptions} from '../components/tabOptions/TabOptions';
import {TabSettings} from '../components/tabSettings/TabSettings';
import {TabView} from '../models/Tabs';

interface ViewSwitchContainerProps {
  children: any;
  tabView: TabView;
}

export const TabsContainer = (props: ViewSwitchContainerProps) => {
  const {children, tabView} = props;
  const Tabs = children;
  const SelectedTab = children.filter(child => child.props.tabName === tabView.selectedTab);
  const TabContent = SelectedTab[0].props.children;
  const TabModeHeaders = TabContent.props.children;

  return (
  <Column>
    <Row>
      {Tabs}
      <TabOptions headers={TabModeHeaders}/>
      <TabSettings/>
    </Row>
    {TabContent}
  </Column>
  );
};

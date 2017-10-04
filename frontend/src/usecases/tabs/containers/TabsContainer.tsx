import * as React from 'react';
import {Column} from '../../layouts/components/column/Column';
import {Row} from '../../layouts/components/row/Row';
import {TabSettings} from '../components/tabSettings/TabSettings';
import {TabView} from '../models/Tabs';
import {TabOptionsContainer} from './TabOptionsContainer';

interface TabsContainerProps {
  children: any;
  tabView: TabView;
}

export const TabsContainer = (props: TabsContainerProps) => {
  const {children, tabView} = props;
  const Tabs = children;
  const SelectedTab = children.filter(child => child.props.tabName === tabView.selectedTab);
  const TabContent = SelectedTab[0].props.children;
  const TabModeHeaders = TabContent.props.children;

  return (
  <Column>
    <Row>
      {Tabs}
      <TabOptionsContainer options={TabModeHeaders}/>
      <TabSettings/>
    </Row>
    {TabContent}
  </Column>
  );
};

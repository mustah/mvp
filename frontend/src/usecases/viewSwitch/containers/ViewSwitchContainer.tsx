import * as React from 'react';
import {Column} from '../../layouts/components/column/Column';
import {Row} from '../../layouts/components/row/Row';
import {TabModes} from '../components/tabModes/TabModes';
import {TabOptions} from '../components/tabOptions/TabOptions';
import {TabView} from '../viewSwitchReducer';

interface ViewSwitchContainerProps {
  children: any;
  tabView: TabView;
}

export const ViewSwitchContainer = (props: ViewSwitchContainerProps) => {
  const {children, tabView} = props;
  const Tabs = children;
  const SelectedTab = children.filter(child => child.props.tabName === tabView.selectedTab);
  const TabContent = SelectedTab[0].props.children;
  const TabModeHeaders = TabContent.props.options;

  return (
  <Column>
    <Row>
      {Tabs}
      <TabModes headers={TabModeHeaders}/>
      <TabOptions/>
    </Row>
    {TabContent}
  </Column>
  );
};

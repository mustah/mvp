import * as React from 'react';
import {Column} from '../../layouts/components/column/Column';
import {Row} from '../../layouts/components/row/Row';
import {TabItemProps} from '../components/tabItem/TabItem';
import {TabSettings} from '../components/tabSettings/TabSettings';
import {TabOptionsContainer} from './TabOptionsContainer';

interface TabsContainerProps {
  children: Array<React.ReactElement<TabItemProps>>;
  selectedTab: string;
}

export const TabsContainer = (props: TabsContainerProps) => {
  const {children, selectedTab} = props;
  const Tabs = children;
  const SelectedTab = children.filter(child => child.props.tabName === selectedTab);
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

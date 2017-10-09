import * as React from 'react';
import 'Tabs.scss';
import {Column} from '../../common/components/layouts/column/Column';
import {Row} from '../../common/components/layouts/row/Row';
import {TabItemProps} from './TabItem';
import {TabOptions} from './TabOptions';
import {TabSettings} from './TabSettings';

interface TabsProps {
  children: Array<React.ReactElement<TabItemProps>>;
  selectedTab: string;
}

export const Tabs = (props: TabsProps) => {
  const {children, selectedTab} = props;
  const Tabs = children;
  const SelectedTab = children.filter(child => child.props.tab === selectedTab);
  const TabContent = SelectedTab[0].props.children;
  const TabModeHeaders = TabContent.props.children;
  return (
    <Column className="Tabs">
      <Row className="Tabs-Row">
        {Tabs}
        <TabOptions options={TabModeHeaders}/>
        <TabSettings/>
      </Row>
      {TabContent}
    </Column>
  );
};

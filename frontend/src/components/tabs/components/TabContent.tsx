import * as React from 'react';
import {TopLevelTab} from '../../../state/ui/tabs/tabsModels';
import {Column} from '../../layouts/column/Column';

export interface TabContentProps {
  tab: TopLevelTab;
  selectedTab: TopLevelTab;
  children: any;
}

export const TabContent = (props: TabContentProps) => {
  const {tab, selectedTab, children} = props;
  if (selectedTab !== tab) {
    return null;
  }
  return (
    <Column className="TabContent">
      {children}
    </Column>
  );
};

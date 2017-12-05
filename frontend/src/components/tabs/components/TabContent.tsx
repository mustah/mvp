import * as React from 'react';
import {TabName} from '../../../state/ui/tabs/tabsModels';
import {Column} from '../../layouts/column/Column';

export interface TabContentProps {
  tab: TabName;
  selectedTab: TabName;
  children: any;
  className?: string;
}

export const TabContent = (props: TabContentProps) => {
  const {tab, selectedTab, children, className} = props;
  if (selectedTab !== tab) {
    return null;
  }
  return (
    <Column className={className}>
      {children}
    </Column>
  );
};

import * as React from 'react';
import {Column} from '../../layouts/components/column/Column';

interface TabContentProps {
  tab: string;
  selectedTab: string;
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

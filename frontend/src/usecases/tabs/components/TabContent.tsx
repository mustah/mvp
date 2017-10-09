import * as React from 'react';
import {Column} from '../../layouts/components/column/Column';
import {TabIdentifier} from '../models/TabsModel';

export interface TabContentProps {
  tab: TabIdentifier;
  selectedTab: TabIdentifier;
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

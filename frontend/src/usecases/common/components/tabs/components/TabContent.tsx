import * as React from 'react';
import {tabType} from '../models/TabsModel';
import {Column} from '../../layouts/column/Column';

export interface TabContentProps {
  tab: tabType;
  selectedTab: tabType;
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

import * as React from 'react';
import {TabName} from '../../../state/ui/tabs/tabsModels';
import {Column} from '../../layouts/column/Column';

export interface TabContentProps {
  tab: TabName;
  selectedTab: TabName;
  children: any;
}

export const TabContent = ({tab, selectedTab, children}: TabContentProps) => {
  return selectedTab === tab ? <Column>{children}</Column> : null;
};
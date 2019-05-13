import {default as classNames} from 'classnames';
import * as React from 'react';
import {TabName} from '../../../state/ui/tabs/tabsModels';
import {ClassNamed} from '../../../types/Types';
import {Column} from '../../layouts/column/Column';

export interface TabContentProps extends ClassNamed {
  tab: TabName;
  selectedTab: TabName;
  children: any;
}

export const TabContent = ({tab, selectedTab, children, className}: TabContentProps) => (
  <Column className={classNames('TabContent', {selected: selectedTab === tab}, className)}>
    {children}
  </Column>
);

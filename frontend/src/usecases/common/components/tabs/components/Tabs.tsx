import * as React from 'react';
import {TabContentProps} from './TabContent';
import {TabTopBarProps} from './TabTopBar';
import {Column} from '../../layouts/column/Column';
import 'Tabs.scss';

type TabsChildren = TabTopBarProps | TabContentProps;

interface TabsProps {
  children: Array<React.ReactElement<TabsChildren>>;
}

export const Tabs = (props: TabsProps) => {
  const {children} = props;
  return (
    <Column className="Tabs">
      {children}
    </Column>
  );
};

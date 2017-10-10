import * as React from 'react';
import 'Tabs.scss';
import {Column} from '../../layouts/components/column/Column';
import {TabContentProps} from './TabContent';
import {TabTopBarProps} from './TabTopBar';

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

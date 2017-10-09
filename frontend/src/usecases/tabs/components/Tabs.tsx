import * as React from 'react';
import 'Tabs.scss';
import {Column} from '../../layouts/components/column/Column';
import {TabContentProps} from './TabContent';
import {TabListProps} from './TabList';

type TabsChildren = TabListProps | TabContentProps;

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

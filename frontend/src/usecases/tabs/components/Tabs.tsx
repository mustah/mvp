import * as React from 'react';
import 'Tabs.scss';
import {Column} from '../../layouts/components/column/Column';

interface TabsProps {
  children: any; // TODO: Make list more specific.
}

export const Tabs = (props: TabsProps) => {
  const {children} = props;
  return (
    <Column className="Tabs">
      {children}
    </Column>
  );
};

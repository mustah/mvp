import * as classNames from 'classnames';
import * as React from 'react';
import 'Tabs.scss';
import {Column} from '../../layouts/column/Column';
import {TabContentProps} from './TabContent';
import {TabTopBarProps} from './TabTopBar';

type TabsChildren = TabTopBarProps | TabContentProps;

interface TabsProps {
  children: Array<React.ReactElement<TabsChildren>>;
  className?: string;
}

export const Tabs = (props: TabsProps) => {
  const {children, className} = props;
  const classes = className ? classNames('Tabs', className) : 'Tabs';
  return (
    <Column className={classes}>
      {children}
    </Column>
  );
};

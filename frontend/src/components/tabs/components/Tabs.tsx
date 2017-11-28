import * as classNames from 'classnames';
import * as React from 'react';
import {Column} from '../../layouts/column/Column';
import {TabContentProps} from './TabContent';
import './Tabs.scss';
import {TabTopBarProps} from './TabTopBar';

type TabsChildren = TabTopBarProps | TabContentProps;

interface TabsProps {
  children: Array<React.ReactElement<TabsChildren>>;
  className?: string;
}

export const Tabs = (props: TabsProps) => {
  const {children, className} = props;
  return (
    <Column className={classNames('Tabs', className)}>
      {children}
    </Column>
  );
};

import * as classNames from 'classnames';
import * as React from 'react';
import {Column} from '../../layouts/components/column/Column';
import {TabContentProps} from '../../validation/containers/ValidationTabsContainer';
import {TabUnderline} from './TabUnderliner';

type TabIdentifier = string;

export interface TabItemProps {
  tabName: string;
  tab: TabIdentifier;
  selectedTab: TabIdentifier;
  changeTab: (tab: string) => void;
  children: React.ReactElement<TabContentProps>;
  // TODO: Should replace any with a type that specifies either LIST, MAP or GRAPH.
}

export const TabItem = (props: TabItemProps) => {
  const {tabName, tab, changeTab, selectedTab} = props;
  const isSelected = selectedTab === tab;
  const onClick = () => changeTab(tab);
  return (
    <Column onClick={onClick} className={'clickable'}>
      <div className={classNames('TabItem', {isSelected})}>
        {tabName}
      </div>
      <TabUnderline isSelected={isSelected}/>
    </Column>
  );
};

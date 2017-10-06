import * as classNames from 'classnames';
import * as React from 'react';
import {Column} from '../../layouts/components/column/Column';
import {TabUnderline} from './TabUnderliner';

type TabIdentifier = string;

export interface TabItemProps {
  tabName: string;
  tab: TabIdentifier;
  selectedTab: TabIdentifier;
  changeTab: (tab: string) => void;
  // TODO: Should replace any with a type that specifies either LIST, MAP or GRAPH.
  children: React.ReactElement<any>;
}

export const TabItem = (props: TabItemProps) => {
  const {tabName, tab, changeTab, selectedTab} = props;
  const isSelected = selectedTab === tab;
  const selectTab = () => changeTab(tab);
  return (
    <Column onClick={selectTab} className="clickable">
      <div className={classNames('TabItem', {isSelected})}>
        {tabName}
      </div>
      <TabUnderline isSelected={isSelected}/>
    </Column>
  );
};

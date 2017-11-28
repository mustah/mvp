import * as classNames from 'classnames';
import * as React from 'react';
import {TopLevelTab} from '../models/TabsModel';
import {TabUnderline} from './TabUnderliner';
import {Column} from '../../layouts/column/Column';

export interface TabProps {
  title: string;
  tab: TopLevelTab;
  selectedTab?: TopLevelTab;
  onChangeTab?: (tab: string) => void;
}

export const Tab = (props: TabProps) => {
  const {title, tab, selectedTab, onChangeTab} = props;
  const selectTab = onChangeTab ? () => onChangeTab(tab) : () => null;
  const isSelected = tab === selectedTab;
  return (
    <Column className={classNames('Tab', {isSelected})} onClick={selectTab}>
      <div className="Tab-header">
        {title}
      </div>
      <TabUnderline isSelected={isSelected}/>
    </Column>
  );
};

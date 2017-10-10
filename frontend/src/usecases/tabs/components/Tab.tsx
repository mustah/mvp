import * as classNames from 'classnames';
import * as React from 'react';
import {Column} from '../../layouts/components/column/Column';
import {tabType} from '../models/TabsModel';
import {TabUnderline} from './TabUnderliner';

export interface TabProps {
  title: string;
  tab: tabType;
  selectedTab: tabType;
  onChangeTab: (tab: string) => void;
}

export const Tab = (props: TabProps) => {
  const {title, tab, selectedTab, onChangeTab} = props;
  const selectTab = () => onChangeTab(tab);
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

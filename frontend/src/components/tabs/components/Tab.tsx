import {default as classNames} from 'classnames';
import * as React from 'react';
import {TabName} from '../../../state/ui/tabs/tabsModels';
import {Column} from '../../layouts/column/Column';
import {FirstUpper} from '../../texts/Texts';
import {TabUnderline} from './TabUnderliner';

export interface TabProps {
  title: string;
  tab: TabName;
  selectedTab?: TabName;
  onChangeTab?: (tab: string) => void;
}

export const Tab = (props: TabProps) => {
  const {title, tab, selectedTab, onChangeTab} = props;
  const selectTab = onChangeTab ? () => onChangeTab(tab) : () => null;
  const isSelected = tab === selectedTab;
  return (
    <Column className={classNames('Tab', {isSelected})} onClick={selectTab}>
      <FirstUpper className="Tab-header">{title}</FirstUpper>
      <TabUnderline isSelected={isSelected}/>
    </Column>
  );
};

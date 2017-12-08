import * as classNames from 'classnames';
import * as React from 'react';
import {TabName} from '../../../state/ui/tabs/tabsModels';
import {TabUnderline} from './TabUnderliner';
import {Column} from '../../layouts/column/Column';

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
      <div className="Tab-header first-uppercase">
        {title}
      </div>
      <TabUnderline isSelected={isSelected}/>
    </Column>
  );
};

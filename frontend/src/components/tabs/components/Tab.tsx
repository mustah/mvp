import {default as classNames} from 'classnames';
import * as React from 'react';
import {TabName} from '../../../state/ui/tabs/tabsModels';
import {CallbackWith} from '../../../types/Types';
import {Column} from '../../layouts/column/Column';
import {Medium} from '../../texts/Texts';
import {TabUnderline} from './TabUnderliner';

export interface TabProps {
  title: string;
  tab: TabName;
  selectedTab?: TabName;
  onChangeTab?: CallbackWith<TabName>;
}

export const Tab = (props: TabProps) => {
  const {title, tab, selectedTab, onChangeTab} = props;
  const selectTab = onChangeTab ? () => onChangeTab(tab) : () => null;
  const isSelected = tab === selectedTab;
  return (
    <Column className={classNames('Tab', {isSelected})} onClick={selectTab}>
      <Medium className="Tab-header Bold first-uppercase">{title}</Medium>
      <TabUnderline isSelected={isSelected}/>
    </Column>
  );
};

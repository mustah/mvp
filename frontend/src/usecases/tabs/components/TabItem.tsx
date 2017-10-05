import * as classNames from 'classnames';
import * as React from 'react';
import {translate} from '../../../services/translationService';
import {Selectable} from '../../../types/Types';
import {Column} from '../../layouts/components/column/Column';
import {TabContentProps} from '../../validation/containers/ValidationTabsContainer';
import {TabUnderline} from './TabUnderliner';

export interface TabItemProps extends Selectable {
  tabName: string;
  changeTab: (tab: string) => void;
  children: React.ReactElement<TabContentProps>;
  // TODO: Should replace any with a type that specifies either LIST, MAP or GRAPH.
}

export const TabItem = (props: TabItemProps) => {
  const {tabName, isSelected, changeTab} = props;
  const onClick = () => changeTab(tabName);

  return (
    <Column onClick={onClick} className={'clickable'}>
      <div className={classNames('TabItem', {isSelected})}>
        {translate(tabName)}
      </div>
      <TabUnderline isSelected={isSelected}/>
    </Column>
  );
};

import * as classNames from 'classnames';
import * as React from 'react';
import 'TabItem.scss';
import {translate} from '../../../../services/translationService';
import {Selectable} from '../../../../types/Types';
import {Column} from '../../../layouts/components/column/Column';
import {TabUnderline} from '../tabUnderline/TabUnderliner';

interface TabItemProps extends Selectable {
  tabName: string;
  changeTab: (tab: string) => void;
}

export const TabItem = (props: TabItemProps) => {
  const {tabName, isSelected, changeTab} = props;
  const onClick = () => {
    changeTab(tabName);
  };

  return (
    <Column onClick={onClick} className={'clickable'}>
      <div className={classNames('TabItem', {isSelected})}>
        {translate(tabName)}
      </div>
      <TabUnderline isSelected={isSelected}/>
    </Column>
  );
};

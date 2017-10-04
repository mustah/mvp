import * as classNames from 'classnames';
import * as React from 'react';
import {Selectable} from '../../../../types/Types';

interface TabOptionProps extends Selectable {
  tab: string;
  tabOptionAction: (tab: string, option: string) => void;
  option: string;
}

export const TabOption = (props: TabOptionProps) => {
  const {tab, tabOptionAction, isSelected, option} = props;
  const onClick = () => {
    tabOptionAction(tab, option);
  };
  return (
    <div className={classNames('TabOption', {isSelected}, 'clickable')} onClick={onClick}>
      {option}
    </div>
  );
};

import * as classNames from 'classnames';
import * as React from 'react';
import {tabType} from '../models/TabsModel';

export interface TabOptionProps {
  tab: tabType;
  select: (tab: tabType, option: string) => void;
  title: string;
  id: string;
  selectedOption: string;
}

export const TabOption = (props: TabOptionProps) => {
  const {tab, select, title, id, selectedOption} = props;
  const isSelected = selectedOption === id;
  const selectTabOption = () => select(tab, id);
  return (
    <div className={classNames('TabOption', {isSelected}, 'clickable')} onClick={selectTabOption}>
      {title}
    </div>
  );
};

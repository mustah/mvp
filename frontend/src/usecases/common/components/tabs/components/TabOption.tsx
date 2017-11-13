import * as classNames from 'classnames';
import * as React from 'react';
import {tabType} from '../models/TabsModel';

export interface TabOptionProps {
  tab?: tabType;
  select?: (tab: tabType, option: string) => void;
  title: React.ReactType;
  id: string;
  selectedOption?: string;
}

export const TabOption = (props: TabOptionProps) => {
  const {tab, select, title, id, selectedOption} = props;
  const isSelected = selectedOption === id;
  const selectTabOption = select && tab ? () => select(tab, id) : () => null;
  return (
    <div className={classNames('TabOption', {isSelected}, 'clickable')} onClick={selectTabOption}>
      {title}
    </div>
  );
};

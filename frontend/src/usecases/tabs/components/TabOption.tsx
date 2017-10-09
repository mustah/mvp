import * as classNames from 'classnames';
import * as React from 'react';
import {TabIdentifier} from '../models/TabsModel';

type OptionIdentifier = string;

export interface TabOptionProps {
  tab: TabIdentifier;
  select: (tab: TabIdentifier, option: string) => void;
  optionName: string;
  option: OptionIdentifier;
  selectedOption: OptionIdentifier;
}

export const TabOption = (props: TabOptionProps) => {
  const {tab, select, optionName, option, selectedOption} = props;
  const isSelected = selectedOption === option;
  const selectTabOption = () => select(tab, option);
  return (
    <div className={classNames('TabOption', {isSelected}, 'clickable')} onClick={selectTabOption}>
      {optionName}
    </div>
  );
};

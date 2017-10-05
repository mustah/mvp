import * as classNames from 'classnames';
import * as React from 'react';

type OptionIdentifier = string;

export interface TabOptionProps {
  tab: string;
  select: (tab: string, option: string) => void;
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

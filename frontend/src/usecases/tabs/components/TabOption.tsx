import * as classNames from 'classnames';
import * as React from 'react';

type OptionIdentifier = string;

export interface TabOptionProps {
  tab: string;
  tabOptionAction: (tab: string, option: string) => void;
  optionName: string;
  option: OptionIdentifier;
  selectedOption: OptionIdentifier;
}

export const TabOption = (props: TabOptionProps) => {
  const {tab, tabOptionAction, optionName, option, selectedOption} = props;
  const isSelected = selectedOption === option;
  const onClick = () => tabOptionAction(tab, option);
  return (
    <div className={classNames('TabOption', {isSelected}, 'clickable')} onClick={onClick}>
      {optionName}
    </div>
  );
};

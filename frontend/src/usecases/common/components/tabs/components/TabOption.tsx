import * as classNames from 'classnames';
import FlatButton from 'material-ui/FlatButton';
import RaisedButton from 'material-ui/RaisedButton';
import * as React from 'react';
import {colors} from '../../../../app/themes';
import {tabType} from '../models/TabsModel';

export interface TabOptionProps {
  className?: string;
  tab?: tabType;
  select?: (tab: tabType, option: string) => void;
  title: React.ReactType;
  id: string;
  selectedOption?: string;
}

export const TabOption = (props: TabOptionProps) => {
  const {tab, select, title, id, selectedOption, className} = props;
  const isSelected = selectedOption === id;
  const selectTabOption = select && tab ? () => select(tab, id) : () => null;
  return (
    <div className={classNames('TabOption', {isSelected}, 'clickable', className)} onClick={selectTabOption}>
      {title}
    </div>
  );
};

const statusColors = {
  all: colors.darkBlue,
  ok: colors.darkGreen,
  warnings: colors.orange,
  faults: colors.red,
};

export const RaisedTabOption = (props: TabOptionProps) => {
  const {tab, select, title, id, selectedOption} = props;
  const isSelected = selectedOption === id;
  const selectTabOption = select && tab ? () => select(tab, id) : () => null;

  const bgColor = isSelected ? statusColors[id] : '#fff';
  const fgColor = isSelected ? '#fff' : statusColors[id];

  const inlineStyle = {
    'margin-left': 16,
  };

  if (isSelected) {
    return (
      <RaisedButton
        backgroundColor={bgColor}
        label={title}
        labelColor={fgColor}
        onClick={selectTabOption}
        style={{...inlineStyle, color: fgColor}}
      />
    );
  }
  return (
    <FlatButton
      backgroundColor={bgColor}
      label={title}
      onClick={selectTabOption}
      style={{...inlineStyle, color: fgColor}}
    />
  );
};

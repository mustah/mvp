import * as React from 'react';
import {SelectionListItem} from '../../state/user-selection/userSelectionModels';
import {Normal} from '../texts/Texts';
import {DropdownProps, DropdownSelector} from './DropdownSelector';
import './DropdownSelector.scss';

const renderLabelAtIndex = (index: number, filteredList: SelectionListItem[]) => {
  const {name} = filteredList[index];
  return <Normal>{name}</Normal>;
};

export const SimpleDropdownSelector = (props: DropdownProps) => (
  <DropdownSelector
    {...props}
    renderLabel={renderLabelAtIndex}
    rowHeight={30}
    visibleItems={10}
  />
);

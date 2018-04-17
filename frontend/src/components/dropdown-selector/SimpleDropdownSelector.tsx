import * as React from 'react';
import {SelectionListItem} from '../../state/search/selection/selectionModels';
import {Normal} from '../texts/Texts';
import {DropdownProps, DropdownSelector} from './DropdownSelector';
import './DropdownSelector.scss';

export const SimpleDropdownSelector = (props: DropdownProps) => {

  const renderLabelAtIndex = (index: number, filteredList: SelectionListItem[]) => {
    const {name} = filteredList[index];
    return <Normal>{name}</Normal>;
  };

  return (
    <DropdownSelector
      {...props}
      renderLabel={renderLabelAtIndex}
      rowHeight={30}
      visibleItems={10}
    />
  );
};

import * as React from 'react';
import {SelectionListItem} from '../../../../state/search/selection/selectionModels';
import {Normal} from '../texts/Texts';
import './DropdownSelector.scss';
import {DropdownProps, DropdownSelector} from './DropdownSelector';

export const SimpleDropdownSelector = (props: DropdownProps) => {

  const renderLabel = (index: number, filteredList: SelectionListItem[]) => {
    const {name} = filteredList[index];
    return (
      <Normal>{name}</Normal>
    );
  };

  return (
    <DropdownSelector
      {...props}
      renderLabel={renderLabel}
      rowHeight={30}
      visibleItems={10}
    />
  );
};

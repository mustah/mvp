import * as React from 'react';
import {translate} from '../../services/translationService';
import {SelectionListItem} from '../../state/search/selection/selectionModels';
import {Normal} from '../texts/Texts';
import {DropdownProps, DropdownSelector} from './DropdownSelector';
import './DropdownSelector.scss';

const unknown = 'unknown';

export const SimpleDropdownSelector = (props: DropdownProps) => {

  const renderLabelAtIndex = (index: number, filteredList: SelectionListItem[]) => {
    const {name} = filteredList[index];
    const translatedName = name === unknown ? translate(unknown) : name;
    return <Normal>{translatedName}</Normal>;
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

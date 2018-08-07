import * as React from 'react';
import {dropDownStyle} from '../../app/themes';
import {translate} from '../../services/translationService';
import {ObjectsById} from '../../state/domain-models/domainModels';
import {SelectionListItem} from '../../state/user-selection/userSelectionModels';
import {IdNamed} from '../../types/Types';
import {Normal} from '../texts/Texts';
import {DropdownProps, DropdownSelector} from './DropdownSelector';
import './DropdownSelector.scss';

interface MultiDropdownSelectorProps extends DropdownProps {
  parentSelectionLookup: ObjectsById<IdNamed>;
  parentIdentifier: string;
}

const unknown: string = 'unknown';

export const MultiDropdownSelector = (props: MultiDropdownSelectorProps) => {
  const {parentIdentifier, parentSelectionLookup, ...DropdownProps} = props;

  const renderLabel = (index: number, filteredList: SelectionListItem[]) => {
    const {name} = filteredList[index];
    const parentId: string = filteredList[index][parentIdentifier];
    const {name: parentName} = parentSelectionLookup[parentId];
    const translatedName: string = parentName === unknown ? translate(unknown) : parentName;
    return ([
      <Normal key={1}>{name}</Normal>,
      <div className="first-uppercase" key={2} style={dropDownStyle.parentStyle}>{translatedName}</div>,
    ]);
  };

  return (
    <DropdownSelector
      {...DropdownProps}
      renderLabel={renderLabel}
      rowHeight={40}
      visibleItems={10}
    />
  );
};

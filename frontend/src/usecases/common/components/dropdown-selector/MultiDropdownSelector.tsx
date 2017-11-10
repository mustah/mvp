import * as React from 'react';
import {DomainModel} from '../../../../state/domain-models/geoData/geoDataModels';
import {SelectionListItem} from '../../../../state/search/selection/selectionModels';
import {IdNamed} from '../../../../types/Types';
import {dropDownStyle} from '../../../app/themes';
import {Normal} from '../texts/Texts';
import './DropdownSelector.scss';
import {DropdownProps, DropdownSelector} from './DropdownSelector';

interface MultiDropdownSelectorProps extends DropdownProps {
  parentSelectionLookup: DomainModel<IdNamed>;
  parentIdentifier: string;
}

export const MultiDropdownSelector = (props: MultiDropdownSelectorProps) => {

  const {parentIdentifier, parentSelectionLookup, ...DropdownProps} = props;

  const renderLabel = (index: number, filteredList: SelectionListItem[]) => {
    const {name} = filteredList[index];
    const parentId = filteredList[index][parentIdentifier];
    return ([
      <Normal key={1}>{name}</Normal>,
      <div key={2} style={dropDownStyle.parentStyle}>{parentSelectionLookup[parentId].name}</div>,
    ]);
  };

  return (
    <DropdownSelector
      {...DropdownProps}
      renderLabel={renderLabel}
      rowHeight={40}
      visibleItems={8}
    />
  );
};

import DropDownMenu from 'material-ui/DropDownMenu';
import * as React from 'react';
import {SelectionDropdownItem} from './SelectionDropdownItem';

interface SelectionDropdownProps {
  setFilter: (filter) => void;
}

export const SelectionDropdown = (props: SelectionDropdownProps) => {
  const {setFilter} = props;
  return (
    <DropDownMenu
      anchorOrigin={{horizontal: 'left', vertical: 'bottom'}}
      maxHeight={300}
      multiple={true}
    >
      <SelectionDropdownItem setFilter={setFilter}>Test</SelectionDropdownItem>
      <SelectionDropdownItem setFilter={setFilter}>Test</SelectionDropdownItem>
      <SelectionDropdownItem setFilter={setFilter}>Test</SelectionDropdownItem>
      <SelectionDropdownItem setFilter={setFilter}>Test</SelectionDropdownItem>
    </DropDownMenu>
  );
};

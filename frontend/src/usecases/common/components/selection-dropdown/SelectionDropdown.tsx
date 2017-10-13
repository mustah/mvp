import DropDownMenu from 'material-ui/DropDownMenu';
import * as React from 'react';
import {SelectionDropdownItem} from './SelectionDropdownItem';

interface SelectionDropdownProps {
  filterAction: (filter) => void;
}

export const SelectionDropdown = (props: SelectionDropdownProps) => {
  const {filterAction} = props;
  return (
    <DropDownMenu
      anchorOrigin={{horizontal: 'left', vertical: 'bottom'}}
      maxHeight={300}
      multiple={true}
    >
      <SelectionDropdownItem value="Göteborg" criteria={{area: "Göteborg"}} filterAction={filterAction}/>
      <SelectionDropdownItem value="Kungsbacka" criteria={{area: "Kungsbacka"}} filterAction={filterAction}/>
    </DropDownMenu>
  );
};

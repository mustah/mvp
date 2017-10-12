import * as React from 'react';
import MenuItem from 'material-ui/MenuItem';
import {Row} from '../layouts/row/Row';

interface SelectionDropdownItemProps {
  children: any;
  setFilter: (filter) => void;
}

export const SelectionDropdownItem = (props: SelectionDropdownItemProps) => {
  const {setFilter, children} = props;

  return (
    <MenuItem>
      <Row className="Row-center" onClick={setFilter}>
        {children}
      </Row>
    </MenuItem>
  );
};

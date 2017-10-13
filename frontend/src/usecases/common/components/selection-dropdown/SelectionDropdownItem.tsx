import * as React from 'react';
import MenuItem from 'material-ui/MenuItem';
import {Row} from '../layouts/row/Row';

interface SelectionDropdownItemProps {
  criteria: any;
  filterAction: (filter) => void;
  value: string;
}

export const SelectionDropdownItem = (props: SelectionDropdownItemProps) => {
  const {filterAction, criteria, value} = props;
  const handleFilterClick = () => {
    filterAction(criteria);
  };

  return (
    <MenuItem>
      <Row className="Row-center" onClick={handleFilterClick}>
        {value}
      </Row>
    </MenuItem>
  );
};

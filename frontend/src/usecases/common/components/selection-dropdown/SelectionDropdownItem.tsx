import * as React from 'react';
import MenuItem from 'material-ui/MenuItem';
import {Row} from '../layouts/row/Row';

interface SelectionDropdownItemProps {
  title: any;
  setFilter: (filter) => void;
}

export const SelectionDropdownItem = (props: SelectionDropdownItemProps) => {
  const {setFilter, title} = props;
  const handleFilterClick = () => {
    setFilter(title);
  };

  return (
    <MenuItem>
      <Row className="Row-center" onClick={handleFilterClick}>
        {title}
      </Row>
    </MenuItem>
  );
};

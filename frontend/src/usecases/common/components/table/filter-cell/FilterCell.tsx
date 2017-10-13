import * as React from 'react';

interface FilterCellProps {
  title: string;
  selectionDropdown: any;
}

// TODO: To format the cells in ProblemOverview and make it possible to unfold a dropdown.
export const FilterCell = (props: FilterCellProps) => {
  const {title, selectionDropdown} = props;
  return (
    <div>
      {title}{selectionDropdown}
    </div>
  );
};

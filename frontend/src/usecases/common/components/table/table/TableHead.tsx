import classNames = require('classnames');
import * as React from 'react';

type SortOrder = 'asc' | 'desc';

interface TableHeadProps {
  sortable?: boolean;
  currentSort?: SortOrder;
  children?: string | React.ReactNode[];
}

export const TableHead = (props: TableHeadProps) => {
  const {children, sortable, currentSort} = props;
  const toggle = (order: SortOrder) => order === 'asc' ? ' ▲' : ' ▼';
  // TODO render link here, for switching order
  return (
    <th className={classNames({clickable: sortable})}>
      {children}{sortable && toggle(currentSort!)}
    </th>
  );
};

import * as React from 'react';

type SortOrder = 'asc' | 'desc';

interface TableHeadProps {
  sortable?: boolean;
  currentSort?: SortOrder;
  children?: React.ReactNode[];
}

export const TableHead = (props: TableHeadProps) => {
  const {children, sortable, currentSort} = props;
  const toggle = (order: SortOrder) => order === 'asc' ? ' ▲' : ' ▼';
  // TODO render link here, for switching order
  return <th>{children}{sortable ? toggle(currentSort!) : null}</th>;
};

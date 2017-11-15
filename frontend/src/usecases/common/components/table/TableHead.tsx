import classNames = require('classnames');
import * as React from 'react';
import {Children, ClassNamed} from '../../../../types/Types';

type SortOrder = 'asc' | 'desc';

interface TableHeadProps extends ClassNamed {
  sortable?: boolean;
  currentSort?: SortOrder;
  children?: Children;
}

export const TableHead = (props: TableHeadProps) => {
  const {children, sortable, currentSort} = props;
  const toggle = (order: SortOrder) => order === 'asc' ? ' ▲' : ' ▼';
  // TODO render link here, for switching order
  return (
    <th className={classNames(props.className, {clickable: sortable})}>
      {children}{sortable && toggle(currentSort!)}
    </th>
  );
};

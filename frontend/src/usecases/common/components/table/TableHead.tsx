import classNames = require('classnames');
import * as React from 'react';
import {Children, ClassNamed} from '../../../../types/Types';

type SortOrder = 'asc' | 'desc';

export interface TableHeadProps extends ClassNamed {
  key?: string;
  sortable?: boolean;
  currentSort?: SortOrder;
  children?: Children;
}

export const TableHead = (props: TableHeadProps) => {
  const {children, sortable, currentSort, key} = props;
  const toggle = (order: SortOrder) => order === 'asc' ? ' ▲' : ' ▼';
  // TODO render link here, for switching order
  return (
    <th className={classNames(props.className, {clickable: sortable})} key={key}>
      {children}{sortable && toggle(currentSort!)}
    </th>
  );
};

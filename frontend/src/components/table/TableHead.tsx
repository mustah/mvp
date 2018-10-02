import {default as classNames} from 'classnames';
import * as React from 'react';
import {Children, ClassNamed} from '../../types/Types';

type SortOrder = 'asc' | 'desc';

export interface TableHeadProps extends ClassNamed {
  key?: string;
  sortable?: boolean;
  currentSort?: SortOrder;
  children?: Children;
}

// TODO render link here, for switching order
const toggle = (order: SortOrder) => order === 'asc' ? ' ▲' : ' ▼';

export const TableHead = ({children, sortable, currentSort, key, className}: TableHeadProps) => (
  <th className={classNames(className, {clickable: sortable})} key={key}>
    {children}{sortable && toggle(currentSort!)}
  </th>
);

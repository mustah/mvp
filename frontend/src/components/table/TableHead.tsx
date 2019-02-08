import {default as classNames} from 'classnames';
import * as React from 'react';
import {ClassNamed, WithChildren} from '../../types/Types';

export interface TableHeadProps extends ClassNamed, WithChildren {
  key?: string;
}

export const TableHead = ({children, key, className}: TableHeadProps) => (
  <th className={classNames(className)} key={key}>
    {children}
  </th>
);

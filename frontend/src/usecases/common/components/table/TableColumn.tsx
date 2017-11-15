import * as React from 'react';
import {Children} from '../../../../types/Types';

/**
 * TODO the 'any' for 'header' and return type of 'cell' are
 * solved by us adding (all?!) our React components in an
 * X.d.ts type definition file. I'm too lazy for that.
 *
 * The 'any' argument for 'value' is more proper - we cannot
 * really know what the DTO's properties are.
 */
interface TableColumnProps {
  header: Children;
  cell?: (value: any, index: number) => any;
  id: string;
}

export const TableColumn = (props: TableColumnProps) => {
  return <td/>;
};

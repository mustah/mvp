import * as React from 'react';

interface TableColumnProps {
  header: any;
  cell?: (value: any, index: any) => any;
  id: string;
}

export const TableColumn = (props: TableColumnProps) => {
  return <td/>;
};

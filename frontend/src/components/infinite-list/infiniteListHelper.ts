import {Index, TableCellProps} from 'react-virtualized';

export const nearestPageNumber = (index: number, pageSize: number): number => Math.floor(index / pageSize);

export const renderText = ({dataKey, rowData}: TableCellProps) => rowData[dataKey];

export const rowClassName = ({index}: Index) => index % 2 === 0 ? 'even' : 'odd';

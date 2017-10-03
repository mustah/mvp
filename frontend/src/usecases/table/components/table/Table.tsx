import * as classNames from 'classnames';
import * as React from 'react';
import {translate} from '../../../../services/translationService';
import './Table.scss';

export interface TableColumn {
  index: string;
  // if "formatted" is missing, we don't render that column.
  // this is useful for passing metadata, for example status codes
  formatted?: string;
  renderCell?: (value: any, index: number) => any;
}

interface TableProps {
  columns: TableColumn[];
  rows: any[];
}

export const Table = (props: TableProps) => {
  const {columns, rows} = props;

  const renderCell = (cell, cellIndex) => {
    const decorator = columns[cellIndex % columns.length].renderCell;
    return <td key={cellIndex}>{decorator ? decorator(cell, cellIndex) : cell}</td>;
  };

  const renderRow = (row, rowIndex) => {
    if (!columns[rowIndex % columns.length].formatted) {
      return null;
    }
    return <tr key={rowIndex}>{row.map(renderCell)}</tr>;
  };

  return (
    <table className={classNames('Table')} cellPadding={0} cellSpacing={0}>
      <thead>
      <tr>
        {columns.map((col, index) => col.formatted ? <th key={index}>{translate(col.formatted)}</th> : null)}
      </tr>
      </thead>
      <tbody>
      {rows.map(renderRow)}
      </tbody>
    </table>
  );
};

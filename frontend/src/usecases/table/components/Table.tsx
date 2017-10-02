import * as React from 'react';
import {translate} from '../../../services/translationService';
import * as classNames from 'classnames';
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
  let {columns, rows} = props;

  const renderRow = function (row, rowIndex) {
    if(!columns[rowIndex % columns.length].formatted) {
      return null;
    }
    return <tr>{row.map(function (cell, cellIndex) {
      const decorator = columns[cellIndex % columns.length].renderCell;
      return <td>{decorator ? decorator(cell, cellIndex) : cell}</td>;
    })}</tr>;
  };

  return (
    <table className={classNames('Table')} cellPadding={0} cellSpacing={0}>
      <thead>
      <tr>
        {columns.map(col => col.formatted ? <th>{translate(col.formatted)}</th> : null)}
      </tr>
      </thead>
      <tbody>
      {rows.map(renderRow)}
      </tbody>
    </table>
  );
};
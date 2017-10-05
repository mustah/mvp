import * as classNames from 'classnames';
import * as React from 'react';
import './Table.scss';

interface NormalizedRows {
  byId: object;
  allIds: string[];
}

interface TableProps {
  data: NormalizedRows;
  children: React.ReactNode[];
}

export const Table = (props: TableProps) => {
  const {data, children} = props;

  /**
   * TODO the alternative to 'any' below is to create and maintain our
   * own typescript file (X.d.ts). I don't feel like it's worth the
   * investment when refactoring & renaming things all over.
   */
  const ths = React.Children.map(children, (child: any) => child.props.header);

  const columnCallbacks = {};
  React.Children.forEach(children, (child: any) => {
    const cb = child.props.cell ? child.props.cell : (value) => value;
    columnCallbacks[child.props.id] = cb;
  });
  const orderedColumns: string[] = React.Children.map(children, (child: any) => child.props.id);

  const rows = (data, columns: string[], columnCallbacks) => {
    if (data.allIds.length === 0) {
      return null;
    }

    const renderCell = (column, index, rowId) => {
      const content = data.byId[rowId][column];
      const callback = columnCallbacks[column];
      return <td key={index}>{callback(content)}</td>;
    };

    const renderRow = (rowId) => (
      <tr key={rowId}>
        {columns.map((column, index) => renderCell(column, index, rowId))}
      </tr>);

    return data.allIds.map(renderRow);
  };

  return (
    <table className={classNames('Table')} cellPadding={0} cellSpacing={0}>
      <thead>
      <tr>{ths}</tr>
      </thead>
      <tbody>
      {rows(data, orderedColumns, columnCallbacks!)}
      </tbody>
    </table>
  );
};

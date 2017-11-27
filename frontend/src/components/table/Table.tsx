import * as classNames from 'classnames';
import * as React from 'react';
import {Children, uuid} from '../../types/Types';
import './Table.scss';
import {TableHeadProps} from './TableHead';

export interface NormalizedRows {
  byId: object;
  allIds: uuid[];
}

type RenderCellCallback = (value: any) => Children;

export interface TableColumnProps {
  header: React.ReactElement<TableHeadProps>;
  renderCell: RenderCellCallback;
}

interface TableProps {
  data: NormalizedRows;
  children: Array<React.ReactElement<TableColumnProps>> | React.ReactElement<TableColumnProps>;
}

export const TableColumn = (props: TableColumnProps) => <td/>;

export const Table = (props: TableProps) => {
  const {data, children} = props;

  const columns = Array.isArray(children) ? children : [children];

  const tableHeaders = columns.map((column: React.ReactElement<TableColumnProps>, index: number) => {
    const {header} = column.props;
    const headerProps: TableHeadProps = {...header.props, key: `${index}`};
    return React.cloneElement(header, headerProps);
  });

  const rows = () => {
    if (!data.allIds.length) {
      return null;
    }

    const renderCell = (onRenderCell: RenderCellCallback, id: uuid, index: number) => {
      const item = data.byId[id];
      return <td key={`cell-${id}-${index}`}>{onRenderCell(item)}</td>;
    };

    const cells: RenderCellCallback[] = columns.map(column => column.props.renderCell);

    const renderRows = (id: uuid) => (
      <tr key={id}>
        {cells.map((onRenderCell: RenderCellCallback, index: number) => renderCell(onRenderCell, id, index))}
      </tr>);

    return data.allIds.map(renderRows);
  };

  return (
    <table className={classNames('Table')} cellPadding={0} cellSpacing={0}>
      <thead>
      <tr>
        {tableHeaders}
      </tr>
      </thead>
      <tbody>
      {rows()}
      </tbody>
    </table>
  );
};

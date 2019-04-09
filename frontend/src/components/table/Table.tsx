import {default as classNames} from 'classnames';
import * as React from 'react';
import {ObjectsById} from '../../state/domain-models/domainModels';
import {Children, ClassNamed, Styled, uuid} from '../../types/Types';
import './Table.scss';
import {TableHeadProps} from './TableHead';

type RenderCellCallback = (value: any) => Children;

export interface TableColumnProps {
  header: React.ReactElement<TableHeadProps>;
  renderCell: RenderCellCallback;
  cellClassName?: string;
}

interface TableProps extends ClassNamed, Styled {
  result: uuid[];
  entities: ObjectsById<any>;
  children: Array<React.ReactElement<TableColumnProps>> | React.ReactElement<TableColumnProps>;
}

export const TableColumn = (_: TableColumnProps) => <td/>;

export const Table = ({result, entities, children, className, style}: TableProps) => {

  const columns = Array.isArray(children) ? children : [children];

  const tableHeaders = columns.map((
    {props: {header}}: React.ReactElement<TableColumnProps>,
    index: number,
  ) => {
    const headerProps: TableHeadProps = {...header.props, key: `${index}`};
    return React.cloneElement(header, headerProps);
  });

  const cellRenderFunctions: RenderCellCallback[] =
    columns.map(({props: {renderCell}}) => renderCell);

  const renderCell = (onRenderCell: RenderCellCallback, id: uuid, index: number) => {
    const item = entities[id];
    const className = columns[index].props.cellClassName;
    return <td key={`cell-${id}-${index}`} className={className}>{onRenderCell(item)}</td>;
  };
  const renderRow = (id: uuid) => (
    <tr key={id}>
      {cellRenderFunctions.map((onRenderCell, index) => renderCell(onRenderCell, id, index))}
    </tr>
  );

  const rows = result.length ? result.map(renderRow) : null;

  return (
    <table className={classNames('Table', className)} style={style} cellPadding={0} cellSpacing={0}>
      <thead>
      <tr>
        {tableHeaders}
      </tr>
      </thead>
      <tbody>
      {rows}
      </tbody>
    </table>
  );
};

import * as React from 'react';
import {translate} from '../../../services/translationService';
import {MeteringPoint} from '../../table/components/meteringPoint/MeteringPoint';
import {Status} from '../../table/components/status/Status';
import {Table} from '../../table/components/table/Table';
import {TableHead} from '../../table/components/table/TableHead';
import {TableColumn} from '../../table/components/tableColumn/TableColumn';
import {ListProps} from '../../tabs/models/TabsModel';

export const ValidationList = (props: ListProps) => {

  const {data} = props;
  const renderMeteringPointCell = (value, index) => <MeteringPoint id={value}/>;
  const renderStatusCell = (value, index) =>
    <Status code={value.code} content={value.text} />;

  return (
    <Table data={data}>
      <TableColumn
        id={'id'}
        header={<TableHead>{translate('meter')}</TableHead>}
        cell={renderMeteringPointCell}
      />
      <TableColumn
        id={'type'}
        header={<TableHead>{translate('type')}</TableHead>}
      />
      <TableColumn
        id={'location'}
        header={<TableHead>{translate('location')}</TableHead>}
      />
      <TableColumn
        id={'gateway'}
        header={<TableHead>{translate('gateway')}</TableHead>}
      />
      <TableColumn
        id={'status'}
        header={<TableHead sortable={true} currentSort={'asc'}>{translate('status')}</TableHead>}
        cell={renderStatusCell}
      />
    </Table>
  );
};

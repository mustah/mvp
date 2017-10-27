import * as React from 'react';
import {translate} from '../../../services/translationService';
import {MeteringPoint} from '../../common/components/table/meteringPoint/MeteringPoint';
import {Status} from '../../common/components/table/status/Status';
import {Table} from '../../common/components/table/table/Table';
import {TableHead} from '../../common/components/table/table/TableHead';
import {TableColumn} from '../../common/components/table/tableColumn/TableColumn';
import {ListProps} from '../../common/components/tabs/models/TabsModel';

export const ValidationList = (props: ListProps) => {

  const {data} = props;
  const renderMeteringPointCell = (value, index) => <MeteringPoint id={value}/>;
  const renderStatusCell = (value, index) =>
    <Status code={0} content={value} />; // TODO: Need to redo this function to that it interprets status correctly.

  return (
    <Table data={data}>
      <TableColumn
        id={'id'}
        header={<TableHead>{translate('meter')}</TableHead>}
        cell={renderMeteringPointCell}
      />
      <TableColumn
        id={'manufacturer'}
        header={<TableHead>{translate('manufacturer')}</TableHead>}
      />
      <TableColumn
        id={'medium'}
        header={<TableHead>{translate('medium')}</TableHead>}
      />
      <TableColumn
        id={'gatewayId'}
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

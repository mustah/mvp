import * as React from 'react';
import {translate} from '../../../../services/translationService';
import {IdNamed} from '../../../../types/Types';
import {MeteringPoint} from '../../../metering-point/MeteringPoint';
import {ListProps} from '../tabs/models/TabsModel';
import {Status} from './status/Status';
import {Table} from './table/Table';
import {TableHead} from './table/TableHead';
import {TableColumn} from './tableColumn/TableColumn';

export const MeterList = (props: ListProps) => {

  const {data} = props;
  const renderMeteringPointCell = (value, index) => <MeteringPoint id={value}/>;
  const renderStatusCell = (status: IdNamed) => <Status {...status}/>;

  const statusHeader = (
    <TableHead
      className="TableHead-status"
    >
      {translate('status')}
    </TableHead>
  );

  return (
    <Table data={data}>
      <TableColumn
        id={'id'}
        header={<TableHead className="first">{translate('meter')}</TableHead>}
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
        header={statusHeader}
        cell={renderStatusCell}
      />
      <TableColumn
        id={'statusChanged'}
        header={<TableHead sortable={true} currentSort="desc">{translate('status change')}</TableHead>}
      />
    </Table>
  );
};

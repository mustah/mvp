import * as React from 'react';
import {translate} from '../../../services/translationService';
import {MeteringPoint} from '../../common/components/table/meteringPoint/MeteringPoint';
import {Status} from '../../common/components/table/status/Status';
import {Table} from '../../common/components/table/table/Table';
import {TableHead} from '../../common/components/table/table/TableHead';
import {TableColumn} from '../../common/components/table/tableColumn/TableColumn';
import {ListProps} from '../../common/components/tabs/models/TabsModel';

export const GatewayList = (props: ListProps) => {

  const {data} = props;
  const renderStatusCell = (value, index) => <Status code={0} content={value}/>;
  // TODO abstract out MeteringPoint <> Gateway
  const renderGateway = (value, index) => <MeteringPoint id={value}/>;

  return (
    <Table data={data}>
      <TableColumn
        id={'id'}
        header={<TableHead sortable={true} currentSort={'asc'}>{translate('gateway')}</TableHead>}
        cell={renderGateway}
      />
      <TableColumn
        id={'city'}
        header={<TableHead>{translate('city')}</TableHead>}
      />
      <TableColumn
        id={'address'}
        header={<TableHead>{translate('address')}</TableHead>}
      />
      <TableColumn
        id={'productModel'}
        header={<TableHead>{translate('product model')}</TableHead>}
      />
      <TableColumn
        id={'status'}
        header={<TableHead>{translate('status')}</TableHead>}
        cell={renderStatusCell}
      />
      <TableColumn
        id={'action'}
        header={<TableHead>{translate('action')}</TableHead>}
      />
    </Table>
  );
};

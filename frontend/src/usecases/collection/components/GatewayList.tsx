import * as React from 'react';
import {translate} from '../../../services/translationService';
import {IdNamed} from '../../../types/Types';
import {Status} from '../../common/components/table/status/Status';
import {Table} from '../../common/components/table/table/Table';
import {TableHead} from '../../common/components/table/table/TableHead';
import {TableColumn} from '../../common/components/table/tableColumn/TableColumn';
import {ListProps} from '../../common/components/tabs/models/TabsModel';
import {Gateway} from '../../gateway/Gateway';

export const GatewayList = (props: ListProps) => {
  const {data} = props;

  const renderStatusCell = (value: IdNamed) => <Status {...value}/>;
  const renderGateway = (value) => <Gateway id={value}/>;
  const renderLocation = (value: IdNamed) => value.name;

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
        cell={renderLocation}
      />
      <TableColumn
        id={'address'}
        header={<TableHead>{translate('address')}</TableHead>}
        cell={renderLocation}
      />
      <TableColumn
        id={'productModel'}
        header={<TableHead>{translate('product model')}</TableHead>}
      />
      <TableColumn
        id={'statusChanged'}
        header={<TableHead sortable={true} currentSort="desc">{translate('status change')}</TableHead>}
      />
      <TableColumn
        id={'status'}
        header={<TableHead className="TableHead-status">{translate('status')}</TableHead>}
        cell={renderStatusCell}
      />
      <TableColumn
        id={'action'}
        header={<TableHead>{translate('action')}</TableHead>}
      />
    </Table>
  );
};

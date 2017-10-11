import * as React from 'react';
import {translate} from '../../../services/translationService';
import {Status} from '../../table/components/status/Status';
import {Table} from '../../table/components/table/Table';
import {TableHead} from '../../table/components/table/TableHead';
import {TableColumn} from '../../table/components/tableColumn/TableColumn';
import {ListProps} from '../../tabs/models/TabsModel';

export const CollectionList = (props: ListProps) => {

  const {data} = props;
  const renderStatusCell = (value, index) => <Status code={value.code} content={value.text}/>;

  return (
    <Table data={data}>
      <TableColumn
        id={'id'}
        header={<TableHead>{translate('gateway')}</TableHead>}
      />
      <TableColumn
        id={'product_model'}
        header={<TableHead>{translate('product model')}</TableHead>}
      />
      <TableColumn
        id={'connected_meters'}
        header={<TableHead>{translate('meter', {context: 'plural'})}</TableHead>}
      />
      <TableColumn
        id={'status'}
        header={<TableHead sortable={true} currentSort={'asc'}>{translate('status')}</TableHead>}
        cell={renderStatusCell}
      />
      <TableColumn
        id={'action'}
        header={<TableHead>{translate('action')}</TableHead>}
      />
    </Table>
  );
};

import * as React from 'react';
import {translate} from '../../../../services/translationService';
import {IdNamed} from '../../../../types/Types';
import {MeteringPoint} from '../../../metering-point/MeteringPoint';
import {ListProps} from '../tabs/models/TabsModel';
import {Status} from './status/Status';
import {Table} from './table/Table';
import {TableHead} from './table/TableHead';
import {TableColumn} from './tableColumn/TableColumn';
import {ActionsDropdown} from '../actions-dropdown/ActionsDropdown';
import {Row} from '../layouts/row/Row';

export const MeterList = (props: ListProps) => {

  const {data} = props;
  const renderMeteringPointCell = (value, index) => <MeteringPoint id={value}/>;
  const renderStatusCell = (status: IdNamed) => <Status {...status}/>;
  const renderLocation = (value: IdNamed) => value.name;
  const actions = [translate('export to Excel (.csv)'), translate('export to JSON'), translate('show gateways')];
  const renderEntryActions = (value) => (
    <Row>
      {value}
      <ActionsDropdown className="flex-1 Row-right" actions={actions}/>
    </Row>
  );

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
        cell={renderEntryActions}
      />
    </Table>
  );
};

import * as React from 'react';
import {translate} from '../../../services/translationService';
import {IdNamed} from '../../../types/Types';
import {ActionsDropdown} from '../../common/components/actions-dropdown/ActionsDropdown';
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

  const actions = [
    translate('export to Excel (.csv)'),
    translate('export to JSON'),
    translate('show meters'),
  ];

  const renderEntryActions = (value) => (<span>{value}</span>);

  const renderActionDropdown = () => <ActionsDropdown actions={actions}/>;

  return (
    <Table data={data}>
      <TableColumn
        id={'id'}
        header={<TableHead className="first">{translate('gateway')}</TableHead>}
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
        id={'status'}
        header={<TableHead className="TableHead-status">{translate('status')}</TableHead>}
        cell={renderStatusCell}
      />
      <TableColumn
        id={'statusChanged'}
        header={<TableHead sortable={true} currentSort={'desc'}>{translate('status change')}</TableHead>}
      />
      <TableColumn
        id={'action'}
        header={<TableHead>{translate('action')}</TableHead>}
        cell={renderEntryActions}
      />
      <TableColumn
        id={'action-buttons'}
        header={<TableHead className="actionDropdown">{' '}</TableHead>}
        cell={renderActionDropdown}
      />
    </Table>
  );
};

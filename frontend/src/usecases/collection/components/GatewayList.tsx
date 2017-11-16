import * as React from 'react';
import {translate} from '../../../services/translationService';
import {Gateway as GatewayModel} from '../../../state/domain-models/gateway/gatewayModels';
import {ListActionsDropdown} from '../../common/components/actions-dropdown/ListActionsDropdown';
import {Status} from '../../common/components/status/Status';
import {Table, TableColumn} from '../../common/components/table/Table';
import {TableHead} from '../../common/components/table/TableHead';
import {ListProps} from '../../common/components/tabs/models/TabsModel';
import {Gateway} from './Gateway';

export const GatewayList = (props: ListProps) => {
  const {data} = props;

  const renderStatusCell = (gateway: GatewayModel) => <Status {...gateway.status}/>;
  const renderGateway = (gateway: GatewayModel) => <Gateway gateway={gateway}/>;
  const renderCity = (gateway: GatewayModel) => gateway.city.name;
  const renderAddress = (gateway: GatewayModel) => gateway.address.name;
  const renderFlags = (gateway: GatewayModel) => gateway.flags.map((flag) => flag.title).join(', ');
  const renderActionDropdown = (gateway: GatewayModel) =>
    <ListActionsDropdown item={{id: gateway.id, name: gateway.productModel}}/>;
  const renderStatusChanged = (gateway: GatewayModel) => gateway.statusChanged;
  const renderProductModel = (gateway: GatewayModel) => gateway.productModel;

  return (
    <Table data={data}>
      <TableColumn
        header={<TableHead className="first">{translate('gateway')}</TableHead>}
        renderCell={renderGateway}
      />
      <TableColumn
        header={<TableHead>{translate('city')}</TableHead>}
        renderCell={renderCity}
      />
      <TableColumn
        header={<TableHead>{translate('address')}</TableHead>}
        renderCell={renderAddress}
      />
      <TableColumn
        header={<TableHead>{translate('product model')}</TableHead>}
        renderCell={renderProductModel}
      />
      <TableColumn
        header={<TableHead className="TableHead-status">{translate('status')}</TableHead>}
        renderCell={renderStatusCell}
      />
      <TableColumn
        header={<TableHead sortable={true} currentSort={'desc'}>{translate('status change')}</TableHead>}
        renderCell={renderStatusChanged}
      />
      <TableColumn
        header={<TableHead>{translate('flags')}</TableHead>}
        renderCell={renderFlags}
      />
      <TableColumn
        header={<TableHead className="actionDropdown">{' '}</TableHead>}
        renderCell={renderActionDropdown}
      />
    </Table>
  );
};

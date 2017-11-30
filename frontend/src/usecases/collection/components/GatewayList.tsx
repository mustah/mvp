import * as React from 'react';
import {ListActionsDropdown} from '../../../components/actions-dropdown/ListActionsDropdown';
import {Separator} from '../../../components/separators/Separator';
import {Status} from '../../../components/status/Status';
import {Table, TableColumn} from '../../../components/table/Table';
import {TableHead} from '../../../components/table/TableHead';
import {translate} from '../../../services/translationService';
import {Normalized} from '../../../state/domain-models/domainModels';
import {Gateway as GatewayModel} from '../../../state/domain-models/gateway/gatewayModels';
import {OnClickWithId} from '../../../types/Types';
import {GatewayListItem} from './GatewayListItem';

interface Props {
  selectEntryAdd: OnClickWithId;
}

export const GatewayList = (props: Normalized<GatewayModel> & Props) => {
  const {result, entities, selectEntryAdd} = props;

  const renderGatewayListItem = (gateway: GatewayModel) => <GatewayListItem gateway={gateway}/>;
  const renderStatusCell = (gateway: GatewayModel) => <Status {...gateway.status}/>;
  const renderCity = (gateway: GatewayModel) => gateway.city.name;
  const renderAddress = (gateway: GatewayModel) => gateway.address.name;
  const renderFlags = (gateway: GatewayModel) => gateway.flags.map((flag) => flag.title).join(', ');
  const renderActionDropdown = (gateway: GatewayModel) =>
    <ListActionsDropdown item={{id: gateway.id, name: gateway.productModel}} selectEntryAdd={selectEntryAdd}/>;
  const renderStatusChanged = (gateway: GatewayModel) => gateway.statusChanged || <Separator/>;
  const renderProductModel = (gateway: GatewayModel) => gateway.productModel;

  return (
    <Table result={result} entities={entities}>
      <TableColumn
        header={<TableHead className="first">{translate('gateway')}</TableHead>}
        renderCell={renderGatewayListItem}
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

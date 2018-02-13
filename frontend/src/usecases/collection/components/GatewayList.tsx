import * as React from 'react';
import {ListActionsDropdown} from '../../../components/actions-dropdown/ListActionsDropdown';
import {Separator} from '../../../components/separators/Separator';
import {Status} from '../../../components/status/Status';
import {Table, TableColumn} from '../../../components/table/Table';
import {TableHead} from '../../../components/table/TableHead';
import {translate} from '../../../services/translationService';
import {DomainModel} from '../../../state/domain-models/domainModels';
import {Flag} from '../../../state/domain-models/flag/flagModels';
import {Gateway} from '../../../state/domain-models/gateway/gatewayModels';
import {OnClickWithId} from '../../../types/Types';
import {GatewayListItem} from './GatewayListItem';

interface Props {
  selectEntryAdd: OnClickWithId;
}

export const GatewayList = (props: DomainModel<Gateway> & Props) => {
  const {result, entities, selectEntryAdd} = props;

  const renderGatewayListItem = (gateway: Gateway) => <GatewayListItem gateway={gateway}/>;
  const renderStatusCell = ({status}: Gateway) => <Status {...status}/>;
  const renderCity = ({city}: Gateway) => city.name;
  const renderAddress = ({address}: Gateway) => address.name;
  const renderFlags = ({flags}: Gateway) => flags.map((flag: Flag) => flag.title).join(', ');
  const renderActionDropdown = ({id, productModel}: Gateway) =>
    <ListActionsDropdown item={{id, name: productModel}} selectEntryAdd={selectEntryAdd}/>;
  const renderStatusChanged = ({statusChanged}: Gateway) => statusChanged || <Separator/>;
  const renderProductModel = ({productModel}: Gateway) => productModel;

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

import * as React from 'react';
import {translate} from '../../services/translationService';
import {Normalized} from '../../state/domain-models/domainModels';
import {Flag} from '../../state/domain-models/flag/flagModels';
import {Meter} from '../../state/domain-models/meter/meterModels';
import {OnClickWithId} from '../../types/Types';
import {ListActionsDropdown} from '../actions-dropdown/ListActionsDropdown';
import {Separator} from '../separators/Separator';
import {Status} from '../status/Status';
import {Table, TableColumn} from '../table/Table';
import {TableHead} from '../table/TableHead';
import {MeterListItem} from './MeterListItem';

interface Props {
  selectEntryAdd: OnClickWithId;
}

export const MeterList = (props: Normalized<Meter> & Props) => {
  const {result, entities, selectEntryAdd} = props;

  const renderMeterListItem = (meter: Meter) => <MeterListItem meter={meter}/>;
  const renderStatusCell = ({status}: Meter) => <Status {...status}/>;
  const renderCityName = ({city}: Meter) => city.name;
  const renderAddressName = ({address}: Meter) => address.name;
  const renderFlags = ({flags}: Meter) => flags.map((flag: Flag) => flag.title).join(', ');
  const renderActionDropdown = ({id, manufacturer}: Meter) =>
    <ListActionsDropdown item={{id, name: manufacturer}} selectEntryAdd={selectEntryAdd}/>;
  const renderGatewayId = ({gatewayId}: Meter) => gatewayId;
  const renderManufacturer = ({manufacturer}: Meter) => manufacturer;
  const renderStatusChanged = ({statusChanged}: Meter) => statusChanged || <Separator/>;
  const renderMedium = ({medium}: Meter) => medium;

  return (
    <Table result={result} entities={entities}>
      <TableColumn
        header={<TableHead className="first">{translate('facility')}</TableHead>}
        renderCell={renderMeterListItem}
      />
      <TableColumn
        header={<TableHead>{translate('city')}</TableHead>}
        renderCell={renderCityName}
      />
      <TableColumn
        header={<TableHead>{translate('address')}</TableHead>}
        renderCell={renderAddressName}
      />
      <TableColumn
        header={<TableHead>{translate('manufacturer')}</TableHead>}
        renderCell={renderManufacturer}
      />
      <TableColumn
        header={<TableHead>{translate('medium')}</TableHead>}
        renderCell={renderMedium}
      />
      <TableColumn
        header={<TableHead>{translate('gateway')}</TableHead>}
        renderCell={renderGatewayId}
      />
      <TableColumn
        header={<TableHead className="TableHead-status">{translate('status')}</TableHead>}
        renderCell={renderStatusCell}
      />
      <TableColumn
        header={<TableHead sortable={true} currentSort="desc">{translate('status change')}</TableHead>}
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

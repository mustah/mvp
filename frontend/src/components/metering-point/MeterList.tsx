import * as React from 'react';
import {translate} from '../../services/translationService';
import {Meter} from '../../state/domain-models/meter/meterModels';
import {Separator} from '../separators/Separator';
import {Status} from '../status/Status';
import {Table, TableColumn} from '../table/Table';
import {TableHead} from '../table/TableHead';
import {MeteringPoint} from './MeteringPoint';
import {ListActionsDropdown} from '../actions-dropdown/ListActionsDropdown';
import {OnClickWithId} from '../../types/Types';
import {Normalized} from '../../state/domain-models/domainModels';

interface Props {
  selectEntryAdd: OnClickWithId;
}

export const MeterList = (props: Normalized<Meter> & Props) => {
  const {result, entities, selectEntryAdd} = props;

  const renderMeteringPointCell = (meter: Meter) => <MeteringPoint meter={meter}/>;
  const renderStatusCell = (meter: Meter) => <Status {...meter.status}/>;
  const renderCityName = (meter: Meter) => meter.city.name;
  const renderAddressName = (meter: Meter) => meter.address.name;
  const renderFlags = (meter: Meter) => meter.flags.map((flag) => flag.title).join(', ');
  const renderActionDropdown = (meter: Meter) =>
    <ListActionsDropdown item={{id: meter.id, name: meter.manufacturer}} selectEntryAdd={selectEntryAdd}/>;
  const renderGatewayId = (meter: Meter) => meter.gatewayId;
  const renderManufacturer = (meter: Meter) => meter.manufacturer;
  const renderStatusChanged = (meter: Meter) => meter.statusChanged || <Separator/>;
  const renderMedium = (meter: Meter) => meter.medium;

  return (
    <Table result={result} entities={entities}>
      <TableColumn
        header={<TableHead className="first">{translate('facility')}</TableHead>}
        renderCell={renderMeteringPointCell}
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

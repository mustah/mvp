import * as React from 'react';
import {ListActionsDropdown} from '../../components/actions-dropdown/ListActionsDropdown';
import {WrappedDateTime} from '../../components/dates/WrappedDateTime';
import {MeterListItem} from '../../components/meters/MeterListItem';
import {PaginationControl} from '../../components/pagination-control/PaginationControl';
import {Status} from '../../components/status/Status';
import {Table, TableColumn} from '../../components/table/Table';
import {TableHead} from '../../components/table/TableHead';
import {formatCollectionPercentage} from '../../helpers/formatters';
import {orUnknown} from '../../helpers/translations';
import {translate} from '../../services/translationService';
import {Meter} from '../../state/domain-models-paginated/meter/meterModels';
import {changePaginationPage} from '../../state/ui/pagination/paginationActions';
import {MeterListProps} from './MeterListContainer';

export const MeterList = (
  {
    componentId,
    result,
    entities,
    entityType,
    selectEntryAdd,
    syncWithMetering,
    isFetching,
    pagination,
  }: MeterListProps) => {

  const renderMeterListItem = (meter: Meter) => <MeterListItem meter={meter}/>;
  const renderStatusCell = ({status: {name}}: Meter) => <Status name={name}/>;
  const renderCityName = ({location: {city}}: Meter) => orUnknown(city.name);
  const renderAddressName = ({location: {address}}: Meter) => orUnknown(address.name);
  const renderActionDropdown = ({id, manufacturer}: Meter) => (
    <ListActionsDropdown
      item={{id, name: manufacturer}}
      selectEntryAdd={selectEntryAdd}
      syncWithMetering={syncWithMetering}
    />);
  const renderGatewaySerial = ({gatewaySerial}: Meter) => gatewaySerial;
  const renderManufacturer = ({manufacturer}: Meter) => manufacturer;
  const renderStatusChanged = ({statusChanged}: Meter) =>
    <WrappedDateTime date={statusChanged} hasContent={!!statusChanged}/>;
  const renderMedium = ({medium}: Meter) => medium;
  const renderCollectionStatus = ({collectionPercentage, readIntervalMinutes}: Meter) =>
    formatCollectionPercentage(collectionPercentage, readIntervalMinutes);

  const collectionPercentageHeader = (
    <TableHead className="number">
      {translate('collection percentage')}
    </TableHead>
  );

  const changePage = (page: number) => changePaginationPage({
    entityType,
    componentId,
    page,
  });

  return (
    <div>
      <Table result={result} entities={entities}>
        <TableColumn
          header={<TableHead className="first">{translate('facility')}</TableHead>}
          renderCell={renderMeterListItem}
        />
        <TableColumn
          header={<TableHead>{translate('city')}</TableHead>}
          cellClassName={'first-uppercase'}
          renderCell={renderCityName}
        />
        <TableColumn
          header={<TableHead>{translate('address')}</TableHead>}
          cellClassName={'first-uppercase'}
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
          renderCell={renderGatewaySerial}
        />
        <TableColumn
          cellClassName="number"
          header={collectionPercentageHeader}
          renderCell={renderCollectionStatus}
        />
        <TableColumn
          header={<TableHead className="TableHead-status">{translate('status')}</TableHead>}
          renderCell={renderStatusCell}
        />
        <TableColumn
          header={<TableHead>{translate('status change')}</TableHead>}
          renderCell={renderStatusChanged}
        />
        <TableColumn
          header={<TableHead className="actionDropdown">{' '}</TableHead>}
          renderCell={renderActionDropdown}
        />
      </Table>
      <PaginationControl pagination={pagination} changePage={changePage}/>
    </div>
  );
};

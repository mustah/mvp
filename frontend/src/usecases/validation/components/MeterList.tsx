import * as React from 'react';
import {ListActionsDropdown} from '../../../components/actions-dropdown/ListActionsDropdown';
import {Column} from '../../../components/layouts/column/Column';
import {RowRight} from '../../../components/layouts/row/Row';
import {MeterListItem} from '../../../components/meters/MeterListItem';
import {PaginationControl} from '../../../components/pagination-control/PaginationControl';
import {MeterAlarm} from '../../../components/status/MeterAlarm';
import {Table, TableColumn} from '../../../components/table/Table';
import {TableHead} from '../../../components/table/TableHead';
import {TableInfoText} from '../../../components/table/TableInfoText';
import {ErrorLabel} from '../../../components/texts/ErrorLabel';
import {Normal} from '../../../components/texts/Texts';
import {MeterListProps} from '../../../containers/meters/MeterListContainer';
import {formatCollectionPercentage} from '../../../helpers/formatters';
import {orUnknown} from '../../../helpers/translations';
import {translate} from '../../../services/translationService';
import {Meter} from '../../../state/domain-models-paginated/meter/meterModels';

export const MeterList = (
  {
    componentId,
    changePage,
    result,
    entities,
    entityType,
    selectEntryAdd,
    syncWithMetering,
    isFetching,
    isSuperAdmin,
    pagination,
  }: MeterListProps) => {

  const renderMeterListItem = (meter: Meter) => <MeterListItem meter={meter}/>;
  const renderMeterId = ({address, isReported}: Meter) => (
    <Column>
      <Normal>{address}</Normal>
      <ErrorLabel hasError={isReported}>{translate('reported')}</ErrorLabel>
    </Column>);
  const renderAlarm = ({alarm}: Meter) => <MeterAlarm alarm={alarm}/>;
  const renderCityName = ({location: {city}}: Meter) => orUnknown(city.name);
  const renderAddressName = ({location: {address}}: Meter) => orUnknown(address.name);
  const renderGatewaySerial = ({gatewaySerial}: Meter) => gatewaySerial;
  const renderManufacturer = ({manufacturer}: Meter) => manufacturer;
  const renderActions = ({id, manufacturer}: Meter) => (
    <RowRight className="ActionsDropdown-list">
      <ListActionsDropdown
        item={{id, name: manufacturer}}
        selectEntryAdd={selectEntryAdd}
        syncWithMetering={syncWithMetering}
      />
    </RowRight>
  );

  const renderMedium = ({medium}: Meter) => medium;
  const renderCollectionStatus = ({collectionPercentage, readIntervalMinutes}: Meter) =>
    formatCollectionPercentage(collectionPercentage, readIntervalMinutes, isSuperAdmin);

  const collectionPercentageHeader = (
    <TableHead className="number">
      {translate('collection percentage')}
    </TableHead>
  );

  const onChangePage = (page: number) => changePage({entityType, componentId, page});

  return (
    <div className="MeterList">
      <Table result={result} entities={entities}>
        <TableColumn
          header={<TableHead className="first">{translate('facility')}</TableHead>}
          cellClassName="icon"
          renderCell={renderMeterListItem}
        />
        <TableColumn
          header={<TableHead>{translate('meter id')}</TableHead>}
          renderCell={renderMeterId}
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
          header={<TableHead className="TableHead-status">{translate('alarm')}</TableHead>}
          renderCell={renderAlarm}
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
          header={<TableHead/>}
          renderCell={renderActions}
        />
      </Table>
      <TableInfoText/>
      <PaginationControl pagination={pagination} changePage={onChangePage}/>
    </div>
  );
};

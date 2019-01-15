import {Grid, GridColumn, GridPageChangeEvent, GridSortChangeEvent} from '@progress/kendo-react-grid';
import * as React from 'react';
import '../../../app/kendo.scss';
import {ListActionsDropdown} from '../../../components/actions-dropdown/ListActionsDropdown';
import {Column} from '../../../components/layouts/column/Column';
import {RowRight} from '../../../components/layouts/row/Row';
import {MeterListProps} from '../../../components/meters/MeterListContent';
import {MeterListItem} from '../../../components/meters/MeterListItem';
import {MeterAlarm} from '../../../components/status/MeterAlarm';
import {ErrorLabel} from '../../../components/texts/ErrorLabel';
import {Normal} from '../../../components/texts/Texts';
import {formatCollectionPercentage} from '../../../helpers/formatters';
import {orUnknown} from '../../../helpers/translations';
import {translate} from '../../../services/translationService';
import {Meter} from '../../../state/domain-models-paginated/meter/meterModels';
import {paginationPageSize} from '../../../state/ui/pagination/paginationReducer';

const renderAlarm = ({alarm}: Meter) => <MeterAlarm alarm={alarm}/>;

const renderGatewaySerial = ({gatewaySerial}: Meter) => gatewaySerial;

const renderMedium = ({medium}: Meter) => medium;

const renderMeterListItem = (meter: Meter) => <MeterListItem meter={meter}/>;

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

  const renderMeterId = ({address, isReported}: Meter) => (
    <Column>
      <Normal>{address}</Normal>
      <ErrorLabel hasError={isReported}>{translate('reported')}</ErrorLabel>
    </Column>
  );

  const renderInKendo =
    (renderer) =>
      (props) => <td>{renderer(props.dataItem)}</td>;

  const renderCityName = ({location: {city}}: Meter) => orUnknown(city);

  const renderAddressName = ({location: {address}}: Meter) => orUnknown(address);

  const renderManufacturer = ({manufacturer}: Meter) => orUnknown(manufacturer);

  const renderActions = ({id, manufacturer}: Meter) => (
    <RowRight className="ActionsDropdown-list">
      <ListActionsDropdown
        item={{id, name: manufacturer}}
        selectEntryAdd={selectEntryAdd}
        syncWithMetering={syncWithMetering}
      />
    </RowRight>
  );

  const renderCollectionStatus = ({collectionPercentage, readIntervalMinutes}: Meter) =>
    formatCollectionPercentage(collectionPercentage, readIntervalMinutes, isSuperAdmin);

  const handleKendoPageChange = (event: GridPageChangeEvent) =>
    changePage({entityType, componentId, page: event.page.skip / paginationPageSize});

  const handleKendoSortChange = (event: GridSortChangeEvent) =>
    console.log('sorting..', event);

  console.log('pagination', pagination)
  const data = result
    .slice(
      pagination.page * pagination.size,
      pagination.page * pagination.size + pagination.size
    )
    .map(key => entities[key]);

  return (
    <>
      <Grid
        data={{data: data, total: pagination.totalElements}}

        pageable={{
          buttonCount: 5,
          info: true,
          type: 'numeric',
          pageSizes: true,
          previousNext: true,
        }}
        pageSize={pagination.size}
        take={pagination.size}
        skip={pagination.page * pagination.size}
        onPageChange={handleKendoPageChange}

        sortable={true}
        onSortChange={handleKendoSortChange}

      >
        <GridColumn
          field="facility"
          cell={renderInKendo(renderMeterListItem)}
          title={translate('facility')}
          width={180}
        />
        <GridColumn field="address" cell={renderInKendo(renderMeterId)} title={translate('meter id')}/>
        <GridColumn field="location" cell={renderInKendo(renderCityName)} title={translate('city')}/>
        <GridColumn
          field="location"
          cell={renderInKendo(renderAddressName)}
          title={translate('address')}
          width={180}
        />
        <GridColumn field="manufacturer" cell={renderInKendo(renderManufacturer)} title={translate('manufacturer')}/>
        <GridColumn field="medium" cell={renderInKendo(renderMedium)} title={translate('medium')}/>
        <GridColumn field="alarm" cell={renderInKendo(renderAlarm)} title={translate('alarm')}/>
        <GridColumn field="gateway" cell={renderInKendo(renderGatewaySerial)} title={translate('gateway')}/>
        <GridColumn
          sortable={false}
          cell={renderInKendo(renderCollectionStatus)}
          title={translate('collection percentage')}
        />
        <GridColumn sortable={false} cell={renderInKendo(renderActions)}/>
      </Grid>
    </>
  );
};

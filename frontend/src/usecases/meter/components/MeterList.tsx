import {
  Grid,
  GridCellProps,
  GridColumn,
  GridPageChangeEvent,
  GridPagerSettings,
  GridSortChangeEvent,
  GridSortSettings
} from '@progress/kendo-react-grid';
import * as React from 'react';
import {borderRadius} from '../../../app/themes';
import {ListActionsDropdown} from '../../../components/actions-dropdown/ListActionsDropdown';
import {ConfirmDialog} from '../../../components/dialog/DeleteConfirmDialog';
import {Column} from '../../../components/layouts/column/Column';
import {RowRight} from '../../../components/layouts/row/Row';
import {MeterListProps} from '../../../components/meters/MeterListContent';
import {MeterListItem} from '../../../components/meters/MeterListItem';
import {MeterAlarm} from '../../../components/status/MeterAlarm';
import {ErrorLabel} from '../../../components/texts/ErrorLabel';
import {Normal} from '../../../components/texts/Texts';
import {formatCollectionPercentage} from '../../../helpers/formatters';
import {orUnknown} from '../../../helpers/translations';
import {firstUpper, firstUpperTranslated, translate} from '../../../services/translationService';
import {ApiRequestSortingOptions} from '../../../state/ui/pagination/paginationModels';
import {paginationPageSize} from '../../../state/ui/pagination/paginationReducer';

const renderAlarm = ({dataItem: {alarm}}: GridCellProps) => <td><MeterAlarm alarm={alarm}/></td>;

const renderMeterListItem = ({dataItem}: GridCellProps) => <td><MeterListItem meter={dataItem}/></td>;

const gridStyle: React.CSSProperties = {
  borderTopWidth: 0,
  borderBottomWidth: 0,
  maxHeight: '1000px',
  marginBottom: borderRadius,
  borderBottomLeftRadius: borderRadius,
  borderBottomRightRadius: borderRadius,
};

const sortable: GridSortSettings = {
  allowUnsort: true,
  mode: 'single'
};

export const MeterList = (
  {
    componentId,
    changePage,
    deleteMeter,
    result,
    entities,
    entityType,
    selectEntryAdd,
    syncWithMetering,
    isFetching,
    isSuperAdmin,
    pagination,
    page,
    sort,
    sortTable,
  }: MeterListProps) => {

  const renderMeterId = ({dataItem: {address, isReported}}: GridCellProps) => (
    <td>
      <Column>
        <Normal>{address}</Normal>
        <ErrorLabel hasError={isReported}>{translate('reported')}</ErrorLabel>
      </Column>
    </td>
  );

  const renderCityName = ({dataItem: {location: {city}}}: GridCellProps) =>
    <td>{firstUpper(orUnknown(city))}</td>;

  const renderAddressName = ({dataItem: {location: {address}}}: GridCellProps) =>
    <td>{firstUpper(orUnknown(address))}</td>;

  const renderManufacturer = ({dataItem: {manufacturer}}: GridCellProps) =>
    <td>{firstUpper(orUnknown(manufacturer))}</td>;

  const renderActions = ({dataItem: {id, manufacturer, facility}}: GridCellProps) => {
    // TODO[!must!] this will be replaced by the common hook for confirm dialogs, created in another branch
    const [isOpen, setOpen] = React.useState<boolean>(false);
    const item = {id, name: manufacturer};
    const openDialog = () => setOpen(true);
    const closeDialog = () => setOpen(false);
    const confirm = () => {
      closeDialog();
      deleteMeter(id, page);
    };
    return (
      <td>
        <RowRight className="ActionsDropdown-list">
          <ListActionsDropdown
            item={item}
            deleteMeter={openDialog}
            selectEntryAdd={selectEntryAdd}
            syncWithMetering={syncWithMetering}
          />
          <ConfirmDialog
            isOpen={isOpen}
            close={closeDialog}
            confirm={confirm}
            text={firstUpperTranslated('are you sure you want to delete the meter {{facility}}', {facility})}
          />
        </RowRight>
      </td>
    );
  };

  const renderCollectionStatus = ({dataItem: {collectionPercentage, readIntervalMinutes}}: GridCellProps) =>
    <td>{formatCollectionPercentage(collectionPercentage, readIntervalMinutes, isSuperAdmin)}</td>;

  const handleKendoPageChange = ({page: {skip}}: GridPageChangeEvent) =>
    changePage({
      entityType,
      componentId,
      page: skip / paginationPageSize
    });

  const handleKendoSortChange = ({sort}: GridSortChangeEvent) => sortTable(sort as ApiRequestSortingOptions[]);

  const data = result.map((key) => entities[key]);

  const pageable: GridPagerSettings = {
    buttonCount: 5,
    info: false,
    type: 'numeric',
    pageSizes: false,
    previousNext: true,
  };

  return (
    <>
      <Grid
        data={{data, total: pagination.totalElements}}

        pageable={pageable}
        pageSize={pagination.size}
        take={pagination.size}
        skip={pagination.page * pagination.size}
        onPageChange={handleKendoPageChange}

        sortable={sortable}
        onSortChange={handleKendoSortChange}
        sort={sort}

        scrollable="none"
        style={gridStyle}
      >
        <GridColumn
          field="facility"
          cell={renderMeterListItem}
          title={translate('facility')}
          width={180}
          headerClassName="left-most"
        />

        <GridColumn field="secondaryAddress" cell={renderMeterId} title={translate('meter id')}/>

        <GridColumn field="city" cell={renderCityName} title={translate('city')}/>

        <GridColumn field="address" cell={renderAddressName} title={translate('address')} width={180}/>

        <GridColumn field="manufacturer" cell={renderManufacturer} title={translate('manufacturer')}/>

        <GridColumn field="medium" title={translate('medium')} width={180} />

        <GridColumn field="alarm" sortable={false} cell={renderAlarm} title={translate('alarm')}/>

        <GridColumn field="gatewaySerial" title={translate('gateway')}/>

        <GridColumn
          sortable={false}
          cell={renderCollectionStatus}
          title={translate('collection percentage')}
        />

        <GridColumn sortable={false} cell={renderActions}/>
      </Grid>
    </>
  );
};

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
import {gridStyle} from '../../../app/themes';
import {ListActionsDropdown} from '../../../components/actions-dropdown/ListActionsDropdown';
import {ConfirmDialog} from '../../../components/dialog/DeleteConfirmDialog';
import {Column} from '../../../components/layouts/column/Column';
import {RowRight} from '../../../components/layouts/row/Row';
import {MeterListProps} from '../../../components/meters/MeterListContent';
import {MeterListItem} from '../../../components/meters/MeterListItem';
import {MeterAlarm} from '../../../components/status/MeterAlarm';
import {Normal} from '../../../components/texts/Texts';
import {orUnknown} from '../../../helpers/translations';
import {firstUpper, firstUpperTranslated, translate} from '../../../services/translationService';
import {ApiRequestSortingOptions} from '../../../state/ui/pagination/paginationModels';
import {paginationPageSize} from '../../../state/ui/pagination/paginationReducer';

const renderAlarm = ({dataItem: {alarm}}: GridCellProps) => <td><MeterAlarm items={alarm}/></td>;

const renderMeterListItem = ({dataItem}: GridCellProps) => <td><MeterListItem meter={dataItem}/></td>;

const pageable: GridPagerSettings = {
  buttonCount: 5,
  info: false,
  type: 'numeric',
  pageSizes: false,
  previousNext: true,
};

const sortable: GridSortSettings = {
  allowUnsort: true,
  mode: 'single'
};

export const MeterList = ({
  componentId,
  changePage,
  deleteMeter,
  result,
  entities,
  entityType,
  addToReport,
  syncWithMetering,
  pagination: {page, size, totalElements: total},
  sort,
  sortTable,
}: MeterListProps) => {

  const renderMeterId = ({dataItem: {address}}: GridCellProps) => (
    <td>
      <Column>
        <Normal>{address}</Normal>
      </Column>
    </td>
  );

  const renderIsReported = ({dataItem: {isReported}}: GridCellProps) => (
    <td><MeterAlarm items={isReported}/></td>
  );

  const renderCityName = ({dataItem: {location: {city}}}: GridCellProps) =>
    <td>{firstUpper(orUnknown(city))}</td>;

  const renderAddressName = ({dataItem: {location: {address}}}: GridCellProps) =>
    <td>{firstUpper(orUnknown(address))}</td>;

  const renderManufacturer = ({dataItem: {manufacturer}}: GridCellProps) =>
    <td>{firstUpper(orUnknown(manufacturer))}</td>;

  const renderActions = ({dataItem}: GridCellProps) => {
    // TODO[!must!] this will be replaced by the common hook for confirm dialogs, created in another branch
    const [isOpen, setOpen] = React.useState<boolean>(false);
    const {id, facility} = dataItem;
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
            item={dataItem}
            deleteMeter={openDialog}
            addToReport={addToReport}
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

  const handlePageChange = ({page: {skip}}: GridPageChangeEvent) =>
    changePage({
      entityType,
      componentId,
      page: skip / paginationPageSize
    });

  const handleSortChange = ({sort}: GridSortChangeEvent) => sortTable(sort as ApiRequestSortingOptions[]);

  const data = result.map((key) => entities[key]);

  const gridData = {data, total};

  return (
    <Grid
      data={gridData}

      pageable={total > size ? pageable : undefined}
      pageSize={size}
      take={size}
      skip={page * size}
      onPageChange={handlePageChange}

      sortable={sortable}
      onSortChange={handleSortChange}
      sort={sort}

      scrollable="none"
      style={gridStyle}
    >
      <GridColumn
        field="facility"
        cell={renderMeterListItem}
        title={translate('facility')}
        headerClassName="left-most"
      />

      <GridColumn field="secondaryAddress" cell={renderMeterId} title={translate('meter id')}/>

      <GridColumn field="city" cell={renderCityName} title={translate('city')}/>

      <GridColumn field="address" cell={renderAddressName} title={translate('address')}/>

      <GridColumn field="manufacturer" cell={renderManufacturer} title={translate('manufacturer')} width={112}/>

      <GridColumn field="medium" title={translate('medium')} width={112}/>

      <GridColumn field="reported" cell={renderIsReported} title={translate('reported')} width={112}/>

      <GridColumn field="alarm" cell={renderAlarm} title={translate('alarm')} width={112}/>

      <GridColumn field="gatewaySerial" title={translate('gateway')} width={112}/>

      <GridColumn sortable={false} cell={renderActions} width={30}/>
    </Grid>
  );
};

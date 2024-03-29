import * as React from 'react';
import {Column, Size, Table, TableCellProps} from 'react-virtualized';
import {makeVirtualizedGridClassName} from '../../../app/themes';
import {ListActionsDropdown} from '../../../components/actions-dropdown/ListActionsDropdown';
import {useConfirmDialog} from '../../../components/dialog/confirmDialogHook';
import {ConfirmDialog} from '../../../components/dialog/DeleteConfirmDialog';
import {ThemeContext} from '../../../components/hoc/withThemeProvider';
import {
  ContentProps,
  InfiniteList,
  InfiniteListProps,
  StateToProps
} from '../../../components/infinite-list/InfiniteList';
import {makeSortingProps, renderText, rowClassName} from '../../../components/infinite-list/infiniteListHelper';
import {RowLeft} from '../../../components/layouts/row/Row';
import {renderLoadingOr} from '../../../components/loading/Loading';
import {MeterDispatchToProps} from '../../../components/meters/MeterGrid';
import {MeterLink} from '../../../components/meters/MeterLink';
import {AlarmStatus} from '../../../components/status/MeterAlarms';
import {orUnknown} from '../../../helpers/translations';
import {firstUpper, firstUpperTranslated, translate} from '../../../services/translationService';
import {Meter} from '../../../state/domain-models-paginated/meter/meterModels';
import {EncodedUriParameters, uuid} from '../../../types/Types';
import {facilitySortOptions, OwnProps} from '../meterModels';

const renderMeterListItem = ({rowData: {facility, id}}: TableCellProps) => <MeterLink facility={facility} id={id}/>;
const renderAlarm = ({rowData}: TableCellProps) => <AlarmStatus hasAlarm={rowData.alarm!}/>;
const renderCity = ({rowData}: TableCellProps) => firstUpper(orUnknown(rowData.location.city));
const renderAddress = ({rowData}: TableCellProps) => firstUpper(orUnknown(rowData.location.address));

export interface MeterListProps extends StateToProps<Meter> {
  legendItemsParameters: EncodedUriParameters;
}

export type Props = MeterListProps & MeterDispatchToProps & OwnProps ;

export const MeterList = ({
  addToReport,
  cssStyles,
  changePage,
  deleteMeter,
  entityType,
  isFetching,
  items,
  pagination,
  paddingBottom,
  selectedItemId,
  sort,
  sortTable,
  syncWithMetering,
}: Props & ThemeContext) => {
  const {page} = pagination;
  const {closeConfirm, confirm, id, isOpen, openConfirm} = useConfirmDialog((id: uuid) => deleteMeter(id, page));

  const renderActionsCell = ({rowData}: TableCellProps) => {
    const {facility, id: meterId} = rowData;
    return (
      <RowLeft>
        <ListActionsDropdown
          item={rowData}
          deleteMeter={openConfirm}
          addToReport={addToReport}
          syncWithMetering={syncWithMetering}
        />
        <ConfirmDialog
          isOpen={isOpen && id === meterId}
          close={closeConfirm}
          confirm={confirm}
          text={firstUpperTranslated('are you sure you want to delete the meter {{facility}}', {facility})}
        />
      </RowLeft>
    );
  };

  const renderContent = ({hasItem, rowHeight, scrollProps, ...props}: ContentProps) =>
    (size: Size) => (
      <Table
        className={makeVirtualizedGridClassName(cssStyles)}
        headerHeight={rowHeight}
        rowHeight={rowHeight}
        rowClassName={rowClassName}
        {...size}
        {...props}
        {...scrollProps}
        {...makeSortingProps({sort, sortTable, sortOptions: facilitySortOptions})}
      >
        <Column
          cellRenderer={renderLoadingOr(hasItem, renderMeterListItem)}
          headerClassName="left-most"
          dataKey="facility"
          label={translate('facility')}
          minWidth={148}
          width={400}
        />
        <Column
          cellRenderer={renderLoadingOr(hasItem, renderText)}
          dataKey="address"
          label={translate('meter id')}
          minWidth={128}
          width={200}
        />
        <Column
          cellRenderer={renderLoadingOr(hasItem, renderCity)}
          className="first-uppercase"
          dataKey="city"
          label={translate('city')}
          minWidth={128}
          width={300}
        />
        <Column
          cellRenderer={renderLoadingOr(hasItem, renderAddress)}
          dataKey="streetAddress"
          label={translate('address')}
          minWidth={138}
          width={600}
        />
        <Column
          cellRenderer={renderLoadingOr(hasItem, renderText)}
          className="first-uppercase"
          dataKey="manufacturer"
          label={translate('manufacturer')}
          minWidth={100}
          width={200}
        />
        <Column
          cellRenderer={renderLoadingOr(hasItem, renderText)}
          dataKey="medium"
          label={translate('medium')}
          minWidth={100}
          width={200}
        />
        <Column
          cellRenderer={renderLoadingOr(hasItem, renderAlarm)}
          dataKey="alarm"
          label={translate('alarm')}
          minWidth={80}
          width={100}
        />
        <Column
          cellRenderer={renderLoadingOr(hasItem, renderText)}
          dataKey="gatewaySerial"
          label={translate('gateway')}
          minWidth={80}
          width={124}
        />
        <Column
          cellRenderer={renderLoadingOr(hasItem, renderActionsCell)}
          className="ListItemActionButtons"
          dataKey="listItemActionButtons"
          minWidth={38}
          width={38}
        />
      </Table>
    );

  const infiniteListProps: InfiniteListProps = {
    changePageTo: (page: number) => changePage({entityType, page}),
    isFetching,
    items,
    paddingBottom,
    pagination,
    renderContent,
    rowHeight: 52,
    selectedItemId,
  };

  return <InfiniteList {...infiniteListProps}/>;
};

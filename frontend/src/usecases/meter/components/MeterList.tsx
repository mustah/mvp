import {first} from 'lodash';
import * as React from 'react';
import {Column, Size, Table, TableCellProps} from 'react-virtualized';
import {makeVirtualizedGridClassName} from '../../../app/themes';
import {ListActionsDropdown} from '../../../components/actions-dropdown/ListActionsDropdown';
import {useConfirmDialog} from '../../../components/dialog/confirmDialogHook';
import {ConfirmDialog} from '../../../components/dialog/DeleteConfirmDialog';
import {ThemeContext} from '../../../components/hoc/withThemeProvider';
import {ContentProps, InfiniteList, InfiniteListProps} from '../../../components/infinite-list/InfiniteList';
import {renderText, rowClassName} from '../../../components/infinite-list/infiniteListHelper';
import {RowRight} from '../../../components/layouts/row/Row';
import {renderLoadingOr} from '../../../components/loading/Loading';
import {DispatchToProps, StateToProps} from '../../../components/meters/MeterGrid';
import {MeterLink} from '../../../components/meters/MeterLink';
import {AlarmStatus} from '../../../components/status/MeterAlarms';
import {Maybe} from '../../../helpers/Maybe';
import {orUnknown} from '../../../helpers/translations';
import {RequestParameter} from '../../../helpers/urlFactory';
import {firstUpper, firstUpperTranslated, translate} from '../../../services/translationService';
import {uuid} from '../../../types/Types';
import {defaultSortProps, OwnProps, SortProps, SortTableProps} from '../meterModels';

const renderMeterListItem = ({rowData: {facility, id}}: TableCellProps) => <MeterLink facility={facility} id={id}/>;
const renderAlarm = ({rowData}: TableCellProps) => <AlarmStatus hasAlarm={rowData.alarm!}/>;
const renderCity = ({rowData}: TableCellProps) => firstUpper(orUnknown(rowData.location.city));
const renderAddress = ({rowData}: TableCellProps) => firstUpper(orUnknown(rowData.location.address));

export type Props = StateToProps & DispatchToProps & OwnProps;

export const MeterList = ({
  cssStyles,
  addToReport,
  changePage,
  deleteMeter,
  isFetching,
  items,
  pagination,
  paddingBottom,
  selectedItemId,
  sortOptions,
  sortMeters,
  syncWithMetering,
}: Props & ThemeContext) => {
  const {page} = pagination;
  const {closeConfirm, confirm, id, isOpen, openConfirm} = useConfirmDialog((id: uuid) => deleteMeter(id, page));

  const renderActionsCell = ({rowData}: TableCellProps) => {
    const {facility, id: meterId} = rowData;
    return (
      <RowRight className="ActionsDropdown-list">
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
      </RowRight>
    );
  };

  const sortProps: SortProps = Maybe.maybe(first(sortOptions))
    .map(({field: sortBy, dir}): SortProps => ({sortBy, sortDirection: dir || 'ASC'}))
    .orElse(defaultSortProps);

  const sortTableProps: SortTableProps = {
    sort: ({sortDirection: dir, sortBy: field}) => sortMeters([{dir, field: field as RequestParameter}]),
    ...sortProps,
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
        {...sortTableProps}
      >
        <Column
          cellRenderer={renderLoadingOr(hasItem, renderMeterListItem)}
          headerClassName="left-most"
          dataKey="facility"
          label={translate('facility')}
          minWidth={168}
          width={400}
        />
        <Column
          cellRenderer={renderLoadingOr(hasItem, renderText)}
          dataKey="address"
          label={translate('meter id')}
          minWidth={168}
          width={200}
        />
        <Column
          cellRenderer={renderLoadingOr(hasItem, renderCity)}
          className="first-uppercase"
          dataKey="city"
          label={translate('city')}
          minWidth={168}
          width={300}
        />
        <Column
          cellRenderer={renderLoadingOr(hasItem, renderAddress)}
          dataKey="streetAddress"
          label={translate('address')}
          minWidth={168}
          width={600}
        />
        <Column
          cellRenderer={renderLoadingOr(hasItem, renderText)}
          className="first-uppercase"
          dataKey="manufacturer"
          label={translate('manufacturer')}
          minWidth={112}
          width={200}
        />
        <Column
          cellRenderer={renderLoadingOr(hasItem, renderText)}
          dataKey="medium"
          label={translate('medium')}
          minWidth={124}
          width={200}
        />
        <Column
          cellRenderer={renderLoadingOr(hasItem, renderAlarm)}
          dataKey="alarm"
          label={translate('alarm')}
          minWidth={112}
          width={112}
        />
        <Column
          cellRenderer={renderLoadingOr(hasItem, renderText)}
          dataKey="gatewaySerial"
          label={translate('gateway')}
          minWidth={112}
          width={112}
        />
        <Column
          cellRenderer={renderLoadingOr(hasItem, renderActionsCell)}
          className="ListItemActionButtons"
          dataKey="listItemActionButtons"
          minWidth={24}
          width={24}
        />
      </Table>
    );

  const infiniteListProps: InfiniteListProps = {
    changePageTo: (page: number) => changePage({entityType: 'meters', page}),
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

import {first} from 'lodash';
import * as React from 'react';
import {
  Alignment,
  AutoSizer,
  Column,
  Index,
  IndexRange,
  InfiniteLoader,
  InfiniteLoaderChildProps,
  InfiniteLoaderProps,
  Size,
  Table,
  TableCellProps,
  TableCellRenderer
} from 'react-virtualized';
import 'react-virtualized/styles.css';
import {colors} from '../../../app/colors';
import {makeVirtualizedGridClassName} from '../../../app/themes';
import {ListActionsDropdown} from '../../../components/actions-dropdown/ListActionsDropdown';
import {useConfirmDialog} from '../../../components/dialog/confirmDialogHook';
import {ConfirmDialog} from '../../../components/dialog/DeleteConfirmDialog';
import {ThemeContext} from '../../../components/hoc/withThemeProvider';
import {Row, RowRight} from '../../../components/layouts/row/Row';
import {DispatchToProps, StateToProps} from '../../../components/meters/MeterGrid';
import {MeterLink} from '../../../components/meters/MeterLink';
import {AlarmStatus} from '../../../components/status/MeterAlarms';
import {Maybe} from '../../../helpers/Maybe';
import {orUnknown} from '../../../helpers/translations';
import {RequestParameter} from '../../../helpers/urlFactory';
import {useResizeWindow} from '../../../hooks/resizeWindowHook';
import {firstUpper, firstUpperTranslated, translate} from '../../../services/translationService';
import {Meter} from '../../../state/domain-models-paginated/meter/meterModels';
import {fillWithNull} from '../../../state/domain-models-paginated/paginatedDomainModelsSelectors';
import {SortDirection} from '../../../state/ui/pagination/paginationModels';
import {CallbackWith, uuid} from '../../../types/Types';

const loaderHeight = 14;
const rowHeight = 52;
const minWidth = 168;

const loaderStyle: React.CSSProperties = {
  borderRadius: loaderHeight / 2,
  backgroundColor: colors.dividerColor,
  height: loaderHeight,
  width: '100%',
};

interface SortProps {
  sortBy: string;
  sortDirection: SortDirection;
}

interface SortTableProps extends SortProps {
  sort: CallbackWith<SortProps>;
}

const defaultSortProps: SortProps = {
  sortBy: RequestParameter.facility,
  sortDirection: 'ASC'
};

interface PageState {
  currentPage: number;
  prevPage: number;
}

export interface OwnProps {
  paddingBottom?: number;
}

export type Props = StateToProps & DispatchToProps & OwnProps;

interface TableScrollProps {
  scrollToIndex?: number;
  scrollToAlignment?: Alignment;
}

const nearestPageNumber = (index: number, pageSize: number): number => Math.floor(index / pageSize);

export const MeterList = ({
  cssStyles,
  addToReport,
  changePage,
  deleteMeter,
  isFetching,
  meters,
  paddingBottom = 276,
  pagination: {page, size, totalPages},
  selectedMeterId,
  sortOptions,
  sortMeters,
  syncWithMetering,
}: Props & ThemeContext) => {
  const scrollToIndex = meters.findIndex(it => it && it.id === selectedMeterId) + 1;
  const [scrollProps, setScrollProps] = React.useState<TableScrollProps>({
    scrollToAlignment: 'center',
    scrollToIndex: selectedMeterId ? scrollToIndex : page * size
  });
  const [pageState, setCurrentPage] = React.useState<PageState>({currentPage: 0, prevPage: page});
  const {height} = useResizeWindow();
  const {closeConfirm, confirm, id, isOpen, openConfirm} = useConfirmDialog((id: uuid) => deleteMeter(id, page));

  const shouldFillEndOfList = isFetching && pageState.currentPage >= pageState.prevPage;
  const list = shouldFillEndOfList ? [...meters, ...fillWithNull({page: 1, fillSize: size})] : meters;

  const getItem = (index: number): Meter | null | undefined => list[index];
  const hasItem = (index: number): boolean => !!getItem(index);
  const numItems: number = list.length;
  const hasNextPage = (page + 1) < totalPages;

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
  const renderMeterListItem = ({rowData: {facility, id}}: TableCellProps) => <MeterLink facility={facility} id={id}/>;
  const renderAlarm = ({rowData}: TableCellProps) => <AlarmStatus hasAlarm={rowData.alarm!}/>;
  const renderCity = ({rowData}: TableCellProps) => firstUpper(orUnknown(rowData.location.city));
  const renderAddress = ({rowData}: TableCellProps) => firstUpper(orUnknown(rowData.location.address));
  const renderText = ({dataKey, rowData}: TableCellProps) => rowData[dataKey];
  const rowClassName = ({index}: Index) => index % 2 === 0 ? 'even' : 'odd';
  const rowGetter = ({index}: Index) => getItem(index) || ({});

  const renderLoadingOr = (cellRenderer: TableCellRenderer) =>
    (props: TableCellProps) => hasItem(props.rowIndex)
      ? cellRenderer(props)
      : (
        <Row style={props.columnIndex === 0 ? {paddingLeft: 14} : {}}>
          <div style={loaderStyle}/>
        </Row>
      );

  const sortTableProps: SortTableProps = {
    sort: ({sortDirection: dir, sortBy: field}) => sortMeters([{dir, field: field as RequestParameter}]),
    ...Maybe.maybe(first(sortOptions))
      .map(({field: sortBy, dir}): SortProps => ({sortBy, sortDirection: dir || 'ASC'}))
      .orElse(defaultSortProps),
  };

  const renderTable = ({onRowsRendered, registerChild}: InfiniteLoaderChildProps) =>
    ({height, width}: Size) => (
      <Table
        className={makeVirtualizedGridClassName(cssStyles)}
        height={height}
        headerHeight={rowHeight}
        onRowsRendered={onRowsRendered}
        overscanRowCount={10}
        ref={registerChild}
        rowHeight={rowHeight}
        rowClassName={rowClassName}
        rowCount={numItems}
        rowGetter={rowGetter}
        width={width}
        {...sortTableProps}
        {...scrollProps}
      >
        <Column
          cellRenderer={renderLoadingOr(renderMeterListItem)}
          headerClassName="left-most"
          dataKey="facility"
          label={translate('facility')}
          minWidth={minWidth}
          width={400}
        />
        <Column
          cellRenderer={renderLoadingOr(renderText)}
          dataKey="address"
          label={translate('meter id')}
          minWidth={minWidth}
          width={200}
        />
        <Column
          cellRenderer={renderLoadingOr(renderCity)}
          className="first-uppercase"
          dataKey="city"
          label={translate('city')}
          minWidth={minWidth}
          width={300}
        />
        <Column
          cellRenderer={renderLoadingOr(renderAddress)}
          dataKey="streetAddress"
          label={translate('address')}
          minWidth={minWidth}
          width={600}
        />
        <Column
          cellRenderer={renderLoadingOr(renderText)}
          className="first-uppercase"
          dataKey="manufacturer"
          label={translate('manufacturer')}
          minWidth={112}
          width={200}
        />
        <Column
          cellRenderer={renderLoadingOr(renderText)}
          dataKey="medium"
          label={translate('medium')}
          minWidth={124}
          width={200}
        />
        <Column
          cellRenderer={renderLoadingOr(renderAlarm)}
          dataKey="alarm"
          label={translate('alarm')}
          width={112}
          minWidth={112}
        />
        <Column
          cellRenderer={renderLoadingOr(renderText)}
          dataKey="gatewaySerial"
          label={translate('gateway')}
          width={112}
          minWidth={112}
        />
        <Column
          cellRenderer={renderLoadingOr(renderActionsCell)}
          className="ListItemActionButtons"
          dataKey="listItemActionButtons"
          width={24}
          minWidth={24}
        />
      </Table>
    );

  const renderContent = (props: InfiniteLoaderChildProps) => (
    <AutoSizer style={{height: height - paddingBottom}}>
      {renderTable(props)}
    </AutoSizer>
  );

  const isRowLoaded = ({index}: Index) => hasItem(index) ? !hasNextPage || index < numItems : false;

  const handlePageChange = (currentPage: number): void => {
    if (!isFetching) {
      setCurrentPage({currentPage, prevPage: page});
      changePage({entityType: 'meters', page: currentPage});
      setScrollProps({});
    }
  };

  const loadMoreRows = ({startIndex}: IndexRange) => new Promise<number>(resolve => {
    const nextPage = nearestPageNumber(startIndex, size);
    handlePageChange(nextPage);
    resolve(nextPage);
  });

  const infiniteLoaderProps: InfiniteLoaderProps = {
    children: renderContent,
    isRowLoaded,
    loadMoreRows,
    rowCount: hasNextPage ? numItems + 1 : numItems,
  };

  return <InfiniteLoader {...infiniteLoaderProps}/>;
};

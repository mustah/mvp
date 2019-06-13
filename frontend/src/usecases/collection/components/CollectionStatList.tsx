import {ExcelExport, ExcelExportColumn} from '@progress/kendo-react-excel-export';
import {first} from 'lodash';
import * as React from 'react';
import {Column, Size, Table, TableCellProps} from 'react-virtualized';
import {makeVirtualizedGridClassName} from '../../../app/themes';
import {ThemeContext} from '../../../components/hoc/withThemeProvider';
import {ContentProps, InfiniteList, InfiniteListProps} from '../../../components/infinite-list/InfiniteList';
import {rowClassName} from '../../../components/infinite-list/infiniteListHelper';
import {renderLoadingOr} from '../../../components/loading/Loading';
import {MeterLink} from '../../../components/meters/MeterLink';
import {formatCollectionPercentage, formatReadInterval} from '../../../helpers/formatters';
import {Maybe} from '../../../helpers/Maybe';
import {RequestParameter} from '../../../helpers/urlFactory';
import {useExportToExcel} from '../../../hooks/exportToExcelHook';
import {translate} from '../../../services/translationService';
import {defaultSortProps, SortProps, SortTableProps} from '../../meter/meterModels';
import {Props} from './CollectionListContent';

const renderMeterListItem = ({rowData: {facility, id}}: TableCellProps) =>
  <MeterLink id={id} facility={facility} subPath={'/collection-period'}/>;

const renderReadInterval = ({rowData: {readInterval}}: TableCellProps) =>
  formatReadInterval(readInterval);

const renderCollectionPercentage = ({rowData: {collectionPercentage, readInterval}}: TableCellProps) =>
  formatCollectionPercentage(collectionPercentage, readInterval);

const save = (exporter: React.RefObject<ExcelExport>) => exporter.current!.save();

export const CollectionStatList = ({
  cssStyles,
  changePage,
  entityType,
  exportToExcelSuccess,
  isExportingToExcel,
  isFetching,
  items,
  pagination,
  selectedItemId,
  sort,
  sortTable,
}: Props & ThemeContext) => {
  const exporter = useExportToExcel({exportToExcelSuccess, isExportingToExcel, save});

  const sortProps: SortProps = Maybe.maybe(first(sort))
    .map(({field: sortBy, dir}): SortProps => ({sortBy, sortDirection: dir || 'ASC'}))
    .orElse(defaultSortProps);

  const sortTableProps: SortTableProps = {
    sort: ({sortDirection: dir, sortBy: field}) => sortTable([{dir, field: field as RequestParameter}]),
    ...sortProps,
  };

  const renderContent = ({hasItem, scrollProps, rowHeight, ...props}: ContentProps) =>
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
          minWidth={200}
          width={300}
        />
        <Column
          cellRenderer={renderLoadingOr(hasItem, renderReadInterval)}
          dataKey="readInterval"
          label={translate('resolution')}
          minWidth={200}
          width={300}
        />
        <Column
          cellRenderer={renderLoadingOr(hasItem, renderCollectionPercentage)}
          dataKey="collectionPercentage"
          label={translate('collection percentage')}
          minWidth={200}
          width={1000}
        />
      </Table>
    );

  const infiniteListProps: InfiniteListProps = {
    changePageTo: (page: number) => changePage({entityType, page}),
    isFetching,
    items,
    paddingBottom: 320,
    pagination,
    renderContent,
    rowHeight: 48,
    selectedItemId,
  };

  const data = items.filter(it => it !== null);

  return (
    <>
      <ExcelExport data={data} ref={exporter} filterable={true} fileName="collection-stats.xlsx">
        <ExcelExportColumn field="facility" title={translate('facility')}/>
        <ExcelExportColumn field="readInterval" title={translate('resolution')}/>
        <ExcelExportColumn field="collectionPercentage" title={translate('collection percentage')}/>
      </ExcelExport>

      <InfiniteList {...infiniteListProps}/>
    </>
  );
};

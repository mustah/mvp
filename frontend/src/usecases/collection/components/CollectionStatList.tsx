import {ExcelExport, ExcelExportColumn} from '@progress/kendo-react-excel-export';
import * as React from 'react';
import {Column, Size, Table, TableCellProps} from 'react-virtualized';
import {makeVirtualizedGridClassName} from '../../../app/themes';
import {ThemeContext} from '../../../components/hoc/withThemeProvider';
import {ContentProps, InfiniteList, InfiniteListProps} from '../../../components/infinite-list/InfiniteList';
import {makeSortingProps, rowClassName} from '../../../components/infinite-list/infiniteListHelper';
import {renderLoadingOr} from '../../../components/loading/Loading';
import {MeterLink} from '../../../components/meters/MeterLink';
import {Separator} from '../../../components/separators/Separator';
import {displayDate} from '../../../helpers/dateHelpers';
import {formatCollectionPercentage, formatReadInterval} from '../../../helpers/formatters';
import {useExportToExcel} from '../../../hooks/exportToExcelHook';
import {translate} from '../../../services/translationService';
import {facilitySortOptions} from '../../meter/meterModels';
import {Props} from './CollectionListContent';

const renderMeterListItem = ({rowData: {facility, id}}: TableCellProps) =>
  <MeterLink id={id} facility={facility} subPath={'/collection-period'}/>;

const renderReadInterval = ({rowData: {readInterval}}: TableCellProps) =>
  formatReadInterval(readInterval);

const renderCollectionPercentage = ({rowData: {collectionPercentage, readInterval}}: TableCellProps) => {
  const displayString = formatCollectionPercentage(collectionPercentage, readInterval);
  return displayString === '-' ? <Separator/> : displayString;
};

const renderLastData = ({rowData: {lastData}}: TableCellProps) =>
  lastData ? displayDate(lastData * 1000) : <Separator/>;

const save = (exporter: React.RefObject<ExcelExport>) => exporter.current!.save();

export const CollectionStatList = ({
  cssStyles,
  changePage,
  entityType,
  exportToExcelSuccess,
  isExportingToExcel,
  isFetching,
  items,
  itemsToExport,
  paddingBottom,
  pagination,
  selectedItemId,
  sort,
  sortTable,
}: Props & ThemeContext) => {
  const exporter = useExportToExcel({
    exportToExcelSuccess,
    isExportingToExcel: isExportingToExcel && itemsToExport.length > 0,
    save
  });

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
        {...makeSortingProps({sort, sortTable, sortOptions: facilitySortOptions})}
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
        <Column
          cellRenderer={renderLoadingOr(hasItem, renderLastData)}
          dataKey="lastData"
          label={translate('last readout')}
          minWidth={200}
          width={1000}
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
    selectedItemId,
  };

  return (
    <>
      <ExcelExport data={itemsToExport} ref={exporter} filterable={true} fileName="collection-stats.xlsx">
        <ExcelExportColumn field="facility" title={translate('facility')}/>
        <ExcelExportColumn field="readInterval" title={translate('resolution')}/>
        <ExcelExportColumn field="collectionPercentage" title={translate('collection percentage')}/>
      </ExcelExport>

      <InfiniteList {...infiniteListProps}/>
    </>
  );
};

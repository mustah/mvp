import {SortDescriptor} from '@progress/kendo-data-query';
import {ExcelExport} from '@progress/kendo-react-excel-export';
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
import {gridStyle, makeGridClassName} from '../../../app/themes';
import {ThemeContext} from '../../../components/hoc/withThemeProvider';
import {MeterLink} from '../../../components/meters/MeterLink';

import {formatCollectionPercentage, formatReadInterval} from '../../../helpers/formatters';
import {RequestParameter} from '../../../helpers/urlFactory';
import {useExportToExcel} from '../../../hooks/exportToExcelHook';
import {translate} from '../../../services/translationService';
import {SortOption} from '../../../state/ui/pagination/paginationModels';
import {paginationPageSize} from '../../../state/ui/pagination/paginationReducer';
import {CollectionListProps} from './CollectionListContent';

interface SortProps {
  sortable?: GridSortSettings;
  onSortChange?: (event: GridSortChangeEvent) => void;
  sort?: SortDescriptor[];
}

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

const renderMeterListItem = ({dataItem: {id, facility}}: GridCellProps) =>
  <td><MeterLink id={id} facility={facility} subPath={'/collection-period'}/></td>;

const renderReadInterval = ({dataItem: {readInterval}}) =>
  <td>{formatReadInterval(readInterval)}</td>;

const renderCollectionPercentage = ({dataItem: {collectionPercentage, readInterval}}) =>
  <td>{formatCollectionPercentage(collectionPercentage, readInterval)}</td>;

const toSortDescriptor = (sortOption: SortOption): SortDescriptor =>
  ({...sortOption, dir: sortOption.dir === 'ASC' ? 'asc' : 'desc'});

const toSortOption = ({field, dir}: SortDescriptor): SortOption =>
  ({field: field as RequestParameter, dir: dir === 'asc' ? 'ASC' : 'DESC'});

const toSortOptions = (sort: SortDescriptor[]): SortOption[] => sort.map(toSortOption);

const toSortDescriptors = (sort): SortDescriptor[] | undefined =>
  sort && sort.length ? sort.map(toSortDescriptor) : undefined;

export const CollectionStatList = ({
  cssStyles,
  changePage,
  exportToExcelSuccess,
  isExportingToExcel,
  result,
  entities,
  entityType,
  pagination: {page, size, totalElements: total},
  sort,
  sortTable,
}: CollectionListProps & ThemeContext) => {
  const exporter = useExportToExcel({
    exportToExcelSuccess,
    isExportingToExcel,
    save: exporter => (exporter as any).current.save()
  });

  const handlePageChange = ({page: {skip}}: GridPageChangeEvent) =>
    changePage({entityType, page: skip / paginationPageSize});

  const scrollProps: SortProps = {
    onSortChange: ({sort}: GridSortChangeEvent) => sortTable(toSortOptions(sort)),
    sort: toSortDescriptors(sort),
    sortable
  };

  const data = result.map(key => entities[key]);

  const gridData = {data, total};

  return (
    <ExcelExport data={data} ref={exporter} filterable={true}>
      <Grid
        className={makeGridClassName(cssStyles)}
        data={gridData}
        pageable={total > size ? pageable : undefined}
        pageSize={size}
        take={size}
        skip={page * size}
        onPageChange={handlePageChange}
        scrollable="none"
        style={gridStyle}
        {...scrollProps}
      >
        <GridColumn
          field="facility"
          title={translate('facility')}
          cell={renderMeterListItem}
          headerClassName="left-most"
          className="left-most"

        />
        <GridColumn
          field="readInterval"
          title={translate('resolution')}
          cell={renderReadInterval}
        />
        <GridColumn
          field="collectionPercentage"
          title={translate('collection percentage')}
          cell={renderCollectionPercentage}
        />
      </Grid>
    </ExcelExport>
  );
};

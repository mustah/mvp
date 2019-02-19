import {
  Grid,
  GridColumn,
  GridPageChangeEvent,
  GridPagerSettings,
  GridSortChangeEvent,
  GridSortSettings
} from '@progress/kendo-react-grid';
import * as React from 'react';
import {gridStyle} from '../../../app/themes';
import {formatCollectionPercentage, formatReadInterval} from '../../../helpers/formatters';
import {translate} from '../../../services/translationService';
import {ApiRequestSortingOptions} from '../../../state/ui/pagination/paginationModels';
import {paginationPageSize} from '../../../state/ui/pagination/paginationReducer';
import {CollectionListProps} from './CollectionListContent';

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

const renderReadInterval = ({dataItem: {readInterval}}) =>
  <td>{formatReadInterval(readInterval)}</td>;

const renderCollectionPercentage = ({dataItem: {collectionPercentage, readInterval}}) =>
  <td>{formatCollectionPercentage(collectionPercentage, readInterval)}</td>;

export const CollectionStatList = ({
  componentId,
  changePage,
  result,
  entities,
  entityType,
  isFetching,
  pagination: {page, size, totalElements: total},
  sort,
  sortTable,
}: CollectionListProps) => {

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
        title={translate('facility')}
        headerClassName="left-most"
        className="left-most"
      />
      <GridColumn
        field="readInterval"
        title={translate('resolution')}
        cell={renderReadInterval}
        sortable={false}
      />
      <GridColumn
        field="collectionPercentage"
        title={translate('collection percentage')}
        cell={renderCollectionPercentage}
      />
    </Grid>
  );
};

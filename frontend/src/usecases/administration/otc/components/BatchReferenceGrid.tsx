import {first} from 'lodash';
import * as React from 'react';
import {Column, Size, Table, TableCellProps} from 'react-virtualized';
import {compose} from 'recompose';
import {colors} from '../../../../app/colors';
import {routes} from '../../../../app/routes';
import {makeVirtualizedGridClassName} from '../../../../app/themes';
import {ButtonSecondary} from '../../../../components/buttons/ButtonSecondary';
import {EmptyContentProps} from '../../../../components/error-message/EmptyContent';
import {withEmptyContent} from '../../../../components/hoc/withEmptyContent';
import {ThemeContext, withCssStyles} from '../../../../components/hoc/withThemeProvider';
import {
  ContentProps,
  DispatchToProps,
  InfiniteList,
  InfiniteListProps,
  StateToProps
} from '../../../../components/infinite-list/InfiniteList';
import {rowClassName} from '../../../../components/infinite-list/infiniteListHelper';
import {renderLoadingOr} from '../../../../components/loading/Loading';
import {displayDate} from '../../../../helpers/dateHelpers';
import {Maybe} from '../../../../helpers/Maybe';
import {RequestParameter} from '../../../../helpers/urlFactory';
import {firstUpperTranslated, translate} from '../../../../services/translationService';
import {BatchReference} from '../../../../state/domain-models-paginated/batch-references/batchReferenceModels';
import {defaultSortProps, SortProps, SortTableProps} from '../../../meter/meterModels';
import {LinkTo} from './LinkTo';

type Props = StateToProps<BatchReference> & DispatchToProps;
type GridProps = Props & ThemeContext;

const renderBatchId = ({rowData: {id}}: TableCellProps) =>
  <LinkTo href={`${routes.otcBatchReferencesModify}/${id}`}>{id.toString()}</LinkTo>;

const renderCreated = ({rowData: {created}}: TableCellProps) => displayDate(created);

const renderStatus = ({rowData: {status}}: TableCellProps) => status;

const renderDevicesButton = ({rowData: {id}}: TableCellProps) => (
  <ButtonSecondary
    label={translate('show devices')}
    style={{backgroundColor: 'transparent', color: colors.notification}}
    title={`Fetch devices for ${id}`}
  />
);

const Grid = ({
  cssStyles,
  changePage,
  entityType,
  isFetching,
  items,
  pagination,
  sort,
  sortTable,
}: GridProps) => {
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
          cellRenderer={renderLoadingOr(hasItem, renderBatchId)}
          headerClassName="left-most"
          dataKey="id"
          label={translate('batch reference')}
          minWidth={200}
          width={300}
        />
        <Column
          cellRenderer={renderLoadingOr(hasItem, renderCreated)}
          dataKey="created"
          label={translate('created')}
          minWidth={200}
          width={300}
        />
        <Column
          cellRenderer={renderLoadingOr(hasItem, renderStatus)}
          dataKey="status"
          label={translate('confirmation status')}
          minWidth={200}
          width={300}
        />
        <Column
          cellRenderer={renderLoadingOr(hasItem, renderDevicesButton)}
          dataKey="id"
          headerClassName="left-most"
          label={translate('devices')}
          minWidth={200}
          width={300}
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
  };

  return <InfiniteList {...infiniteListProps}/>;
};

const GridWrapper = compose<GridProps, Props>(withCssStyles, withEmptyContent)(Grid);

export const BatchReferenceGrid = (props: Props) => {
  const {
    fetchPaginated,
    pagination: {page},
    parameters,
    sort,
  } = props;

  React.useEffect(() => {
    fetchPaginated(page, parameters, sort);
  }, [page, parameters, sort]);

  const wrapperProps: Props & EmptyContentProps = {
    ...props,
    noContentText: firstUpperTranslated('no batch references'),
  };

  return <GridWrapper {...wrapperProps}/>;
};

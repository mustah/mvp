import * as React from 'react';
import {Column, Size, Table, TableCellProps} from 'react-virtualized';
import {compose} from 'recompose';
import {routes} from '../../../../app/routes';
import {makeVirtualizedGridClassName} from '../../../../app/themes';
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
import {makeSortingProps, rowClassName} from '../../../../components/infinite-list/infiniteListHelper';
import {renderLoadingOr} from '../../../../components/loading/Loading';
import {Separator} from '../../../../components/separators/Separator';
import {firstUpperTranslated, translate} from '../../../../services/translationService';
import {Device} from '../../../../state/domain-models-paginated/devices/deviceModels';
import {LinkTo} from './LinkTo';

type Props = StateToProps<Device> & DispatchToProps;
type GridProps = Props & ThemeContext;

const renderId = ({rowData: {id}}: TableCellProps) =>
  <LinkTo href={`${routes.otcDevicesModify}/${id}`}>{id.toString()}</LinkTo>;

const renderDeviceType = ({rowData: {deviceType}}: TableCellProps) => deviceType;

const renderClaimStatus = ({rowData: {claimStatus}}: TableCellProps) => claimStatus;

const renderAttributes = ({rowData: {attributes}}: TableCellProps) =>
  Object.keys(attributes).length > 0 ? attributes : <Separator/>;

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
        {...makeSortingProps({sort, sortTable})}
      >
        <Column
          cellRenderer={renderLoadingOr(hasItem, renderId)}
          headerClassName="left-most"
          dataKey="id"
          label={translate('device eui')}
          minWidth={200}
          width={300}
        />
        <Column
          cellRenderer={renderLoadingOr(hasItem, renderDeviceType)}
          dataKey="deviceType"
          label={translate('object type')}
          minWidth={200}
          width={300}
        />
        <Column
          cellRenderer={renderLoadingOr(hasItem, renderClaimStatus)}
          dataKey="claimStatus"
          label={translate('claim status')}
          minWidth={200}
          width={300}
        />
        <Column
          cellRenderer={renderLoadingOr(hasItem, renderAttributes)}
          dataKey="attributes"
          label={translate('labels')}
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

export const DevicesGrid = (props: Props) => {
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
    noContentText: firstUpperTranslated('no devices'),
  };

  return <GridWrapper {...wrapperProps}/>;
};

import * as React from 'react';
import {
  Alignment,
  AutoSizer,
  Index,
  IndexRange,
  InfiniteLoader,
  InfiniteLoaderChildProps,
  InfiniteLoaderProps,
  Size
} from 'react-virtualized';
import {useResizeWindow} from '../../hooks/resizeWindowHook';
import {Meter} from '../../state/domain-models-paginated/meter/meterModels';
import {fillWithNull} from '../../state/domain-models-paginated/paginatedDomainModelsSelectors';
import {EntityTyped, OnChangePage, Pagination, SortOption} from '../../state/ui/pagination/paginationModels';
import {
  CallbackWith,
  EncodedUriParameters,
  Fetching,
  FetchPaginated,
  HasContent,
  Predicate,
  uuid
} from '../../types/Types';
import {OwnProps} from '../../usecases/meter/meterModels';
import {nearestPageNumber} from './infiniteListHelper';

interface PageState {
  currentPage: number;
  prevPage: number;
}

interface TableScrollProps {
  scrollToIndex?: number;
  scrollToAlignment?: Alignment;
}

export interface StateToProps<T> extends EntityTyped, Fetching, HasContent {
  items: T[];
  pagination: Pagination;
  parameters: EncodedUriParameters;
  selectedItemId?: uuid;
  sort?: SortOption[];
}

export interface DispatchToProps {
  changePage: OnChangePage;
  fetchPaginated: FetchPaginated;
  sortTable: CallbackWith<SortOption[]>;
}

export interface ContentProps extends InfiniteLoaderChildProps {
  hasItem: Predicate<number>;
  rowCount: number;
  rowHeight: number;
  rowGetter: (info: Index) => any;
  scrollProps: TableScrollProps;
}

export interface InfiniteListProps extends OwnProps {
  changePageTo: CallbackWith<number>;
  isFetching: boolean;
  items: any[];
  pagination: Pagination;
  renderContent: (props: ContentProps) => (props: Size) => React.ReactNode;
  rowHeight: number;
  selectedItemId?: uuid;
}

export const InfiniteList = ({
  changePageTo,
  isFetching,
  items,
  paddingBottom = 276,
  pagination: {page, size, totalPages},
  renderContent,
  rowHeight,
  selectedItemId,
}: InfiniteListProps) => {
  const scrollToIndex = items.findIndex(it => it && it.id === selectedItemId) + 1;
  const [scrollProps, setScrollProps] = React.useState<TableScrollProps>({
    scrollToAlignment: 'center',
    scrollToIndex: selectedItemId ? scrollToIndex : page * size
  });
  const [pageState, setCurrentPage] = React.useState<PageState>({currentPage: 0, prevPage: page});
  const {height: innerHeight} = useResizeWindow();

  const shouldFillEndOfList = isFetching && pageState.currentPage >= pageState.prevPage;
  const list = shouldFillEndOfList ? [...items, ...fillWithNull({page: 1, fillSize: size})] : items;

  const getItem = (index: number): Meter | null | undefined => list[index];
  const hasItem = (index: number): boolean => !!getItem(index);
  const rowGetter = ({index}: Index) => getItem(index) || ({});
  const rowCount = list.length;
  const height = rowCount < 5 ? (rowCount + 1) * rowHeight : innerHeight - paddingBottom;

  const renderChildren = (props: InfiniteLoaderChildProps) => (
    <AutoSizer style={{height}}>
      {renderContent({...props, hasItem, scrollProps, rowCount, rowGetter, rowHeight})}
    </AutoSizer>
  );

  const handlePageChange = (currentPage: number): void => {
    if (!isFetching) {
      setCurrentPage({currentPage, prevPage: page});
      changePageTo(currentPage);
      setScrollProps({});
    }
  };

  const loadMoreRows = ({startIndex}: IndexRange) => new Promise<number>(resolve => {
    const nextPage = nearestPageNumber(startIndex, size);
    handlePageChange(nextPage);
    resolve(nextPage);
  });

  const hasNextPage = (page + 1) < totalPages;

  const isRowLoaded = ({index}: Index) => hasItem(index) ? !hasNextPage || index < rowCount : false;

  const infiniteLoaderProps: InfiniteLoaderProps = {
    children: renderChildren,
    isRowLoaded,
    loadMoreRows,
    rowCount: hasNextPage ? rowCount + 1 : rowCount,
  };

  return <InfiniteLoader {...infiniteLoaderProps}/>;
};

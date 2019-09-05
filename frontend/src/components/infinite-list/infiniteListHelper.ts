import {first} from 'lodash';
import {Index, TableCellProps} from 'react-virtualized';
import {Maybe} from '../../helpers/Maybe';
import {RequestParameter} from '../../helpers/urlFactory';
import {SortOption} from '../../state/ui/pagination/paginationModels';
import {CallbackWith} from '../../types/Types';
import {idSortOptions, SortProps} from '../../usecases/meter/meterModels';

export const nearestPageNumber = (index: number, pageSize: number): number => Math.floor(index / pageSize);

export const renderText = ({dataKey, rowData}: TableCellProps) => rowData[dataKey];

export const rowClassName = ({index}: Index) => index % 2 === 0 ? 'even' : 'odd';

interface SortingProps extends SortProps {
  sort: CallbackWith<SortProps>;
}

interface SortTableFactoryProps {
  sort?: SortOption[];
  sortTable: CallbackWith<SortOption[]>;
  sortOptions?: SortProps;
}

export const makeSortingProps =
  ({sort, sortTable, sortOptions = idSortOptions}: SortTableFactoryProps): SortingProps => {
    const sortProps: SortProps = Maybe.maybe(first(sort))
      .map(({field: sortBy, dir}): SortProps => ({sortBy, sortDirection: dir || 'ASC'}))
      .orElse(sortOptions);

    return {
      sort: ({sortDirection: dir, sortBy: field}) => sortTable([{dir, field: field as RequestParameter}]),
      ...sortProps,
    };
  };

import {RequestParameter} from '../../helpers/urlFactory';
import {SortDirection} from '../../state/ui/pagination/paginationModels';
import {CallbackWith} from '../../types/Types';

export interface SortProps {
  sortBy: string;
  sortDirection: SortDirection;
}

export interface SortTableProps extends SortProps {
  sort: CallbackWith<SortProps>;
}

export interface OwnProps {
  paddingBottom?: number;
}

export const defaultSortProps: SortProps = {
  sortBy: RequestParameter.facility,
  sortDirection: 'ASC'
};

import {RequestParameter} from '../../helpers/urlFactory';
import {SortDirection} from '../../state/ui/pagination/paginationModels';

export interface SortProps {
  sortBy: string;
  sortDirection: SortDirection;
}

export interface OwnProps {
  paddingBottom?: number;
}

export const facilitySortOptions: SortProps = {
  sortBy: RequestParameter.facility,
  sortDirection: 'ASC'
};

export const idSortOptions: SortProps = {
  sortBy: RequestParameter.id,
  sortDirection: 'ASC'
};

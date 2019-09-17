import {TotalElements} from '../../domain-models-paginated/paginatedDomainModels';
import {Query} from '../../search/searchModels';
import {SelectionListItem} from '../../user-selection/userSelectionModels';

export interface PagedResponse extends Query, TotalElements {
  items: SelectionListItem[];
}

export type FetchByPage = (page: number, query?: string) => Promise<PagedResponse>;

export interface CityResponse {
  name: string;
  country: string;
}

export interface AddressResponse {
  street: string;
  city: string;
  country: string;
}

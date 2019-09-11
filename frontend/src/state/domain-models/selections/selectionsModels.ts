import {Query} from '../../search/searchModels';
import {SelectionListItem} from '../../user-selection/userSelectionModels';

export interface PagedResponse extends Query {
  items: SelectionListItem[];
  totalElements: number;
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

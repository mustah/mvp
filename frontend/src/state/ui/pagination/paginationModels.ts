import {uuid} from '../../../types/Types';
import {DomainModelsState} from '../../domain-models/domainModels';
import {HasPageNumber} from '../../domain-models/paginatedDomainModels';

export interface PaginationMetadata {
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface HasComponentId {
  componentId: uuid;
}

export type PaginationChangePayload = HasComponentId & HasPageNumber;

export type OnChangePage = (page: number) => void;

interface PaginationModel extends PaginationMetadata {
  useCases: {[component: string]: HasPageNumber};
}

export type PaginationState = {
  [model in keyof DomainModelsState] : PaginationModel
  };

export interface SortingOptions {
  direction: 'ASC' | 'DESC';
  property: string;
  ignoreCase: boolean;
  nullHandling: string;
  ascending: boolean;
  descending: boolean;
}

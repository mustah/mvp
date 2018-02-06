import {uuid} from '../../../types/Types';
import {
  HasPageNumber, NormalizedPaginatedResult,
  PaginatedDomainModelsState,
} from '../../domain-models-paginated/paginatedDomainModels';

export interface PaginationMetadata {
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface HasComponentId {
  componentId: uuid;
}

export type PaginationChangePayload = HasComponentId & HasPageNumber & {model: keyof  PaginatedDomainModelsState};
export type PaginationMetadataPayload = NormalizedPaginatedResult & {model: keyof PaginatedDomainModelsState};

export type OnChangePage = (page: number) => void;

export interface PaginationModel extends PaginationMetadata {
  useCases: {[component: string]: HasPageNumber};
}

// TODO: perhaps make a type of "keyof PaginatedDomainModelsState"
export type PaginationState = {
  [model in keyof PaginatedDomainModelsState] : PaginationModel
  };

export interface SortingOptions {
  direction: 'ASC' | 'DESC';
  property: string;
  ignoreCase: boolean;
  nullHandling: string;
  ascending: boolean;
  descending: boolean;
}

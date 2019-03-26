import {Identifiable} from '../../../types/Types';
import {NormalizedPaginatedState} from '../../domain-models-paginated/paginatedDomainModels';
import {SelectedParameters} from '../../user-selection/userSelectionModels';

export interface CollectionStat extends Identifiable {
  collectionPercentage: number;
  date: number;
  facility: string;
  isExportingToExcel: boolean;
  latency: string;
  readIntervalMinutes: number;
}

export interface CollectionStatParameters {
  selectionParameters: SelectedParameters;
}

export type CollectionStatFacilityState = NormalizedPaginatedState<CollectionStat>;

export type FetchCollectionStats = (requestParameters: string) => void;

import {Identifiable} from '../../../types/Types';
import {NormalizedPaginatedState} from '../../domain-models-paginated/paginatedDomainModels';
import {SelectedParameters} from '../../user-selection/userSelectionModels';

export interface CollectionStat extends Identifiable {
  date: number;
  facility: string;
  readIntervalMinutes: number;
  collectionPercentage: number;
  latency: string;
}

export interface CollectionStatParameters {
  selectionParameters: SelectedParameters;
}

export type CollectionStatFacilityState = NormalizedPaginatedState<CollectionStat>;

export type FetchCollectionStats = (requestParameters: string) => void;

import {ErrorResponse} from '../../types/Types';

export interface SelectionSummary {
  numMeters: number;
  numCities: number;
  numAddresses: number;
}

export interface SummaryState {
  isFetching: boolean;
  isSuccessfullyFetched: boolean;
  payload: SelectionSummary;
  error?: ErrorResponse;
}

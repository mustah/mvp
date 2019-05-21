import {RequestsHttp} from '../domain-models/domainModels';

export interface SelectionSummary {
  numMeters: number;
  numCities: number;
  numAddresses: number;
}

export interface SummaryState extends RequestsHttp {
  payload: SelectionSummary;
}

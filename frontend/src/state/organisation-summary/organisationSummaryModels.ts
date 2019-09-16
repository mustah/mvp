import {RequestsHttp} from '../domain-models/domainModels';

export interface SummaryState extends RequestsHttp {
  numMeters: number;
}

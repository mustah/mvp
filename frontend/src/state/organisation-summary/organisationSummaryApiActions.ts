import {GetState} from '../../reducers/rootReducer';
import {emptyActionOf, Sectors} from '../../types/Types';
import {clearAction, fetchIfNeeded, FetchIfNeeded} from '../api/apiActions';
import {SummaryState} from './organisationSummaryModels';

const shouldFetchOrganisationSummary: FetchIfNeeded = (getState: GetState): boolean => {
  const {isFetching, isSuccessfullyFetched, error}: SummaryState = getState().organisationSummary;
  return !isSuccessfullyFetched && !error && !isFetching;
};

export const fetchOrganisationSummary = fetchIfNeeded<number>(
  Sectors.organisationSummary,
  shouldFetchOrganisationSummary,
);

export const clearOrganisationSummary = emptyActionOf(clearAction(Sectors.organisationSummary));

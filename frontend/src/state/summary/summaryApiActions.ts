import {GetState} from '../../reducers/rootReducer';
import {emptyActionOf, Sectors} from '../../types/Types';
import {clearAction, fetchIfNeeded, FetchIfNeeded} from '../api/apiActions';
import {SelectionSummary, SummaryState} from './summaryModels';

const shouldFetchSummary: FetchIfNeeded = (getState: GetState): boolean => {
  const summary: SummaryState = getState().summary;
  return !summary.isSuccessfullyFetched && !summary.error && !summary.isFetching;
};

const shouldFetchOrganisationSummary: FetchIfNeeded = (getState: GetState): boolean => {
  const organisationSummary: SummaryState = getState().organisationSummary;
  return !organisationSummary.isSuccessfullyFetched && !organisationSummary.error && !organisationSummary.isFetching;
};

export const fetchSummary = fetchIfNeeded<SelectionSummary>(
  Sectors.summary,
  shouldFetchSummary
);

export const fetchOrganisationSummary = fetchIfNeeded<SelectionSummary>(
  Sectors.organisationSummary,
  shouldFetchOrganisationSummary,
);

export const clearOrganisationSummary = emptyActionOf(clearAction(Sectors.organisationSummary));

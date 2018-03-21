import {SelectionSummary, SummaryState} from './summaryModels';

export const getSelectionSummary = (state: SummaryState): SelectionSummary => state.payload;

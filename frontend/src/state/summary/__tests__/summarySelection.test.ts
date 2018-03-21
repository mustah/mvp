import {SummaryState} from '../summaryModels';
import {initialState} from '../summaryReducer';
import {getSelectionSummary} from '../summarySelection';

describe('summarySelection', () => {

  it('handles an empty selection summary', () => {
    expect(getSelectionSummary(initialState)).toEqual({
      numAddresses: 0,
      numCities: 0,
      numMeters: 0,
    });
  });

  it('return the summary in state', () => {
    const meterState: SummaryState = {
      isFetching: false,
      isSuccessfullyFetched: false,
      payload: {numMeters: 2, numCities: 1, numAddresses: 2},
    };

    expect(getSelectionSummary(meterState)).toEqual({numMeters: 2, numCities: 1, numAddresses: 2});
  });

});

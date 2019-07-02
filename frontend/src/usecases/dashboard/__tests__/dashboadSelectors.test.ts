import {RequestsHttp} from '../../../state/domain-models/domainModels';
import {WidgetState} from '../../../state/widget/widgetReducer';
import {getMeterCount, isFetching} from '../dashboardSelectors';

describe('dashboard selectors', () => {

  describe('given empty widget state', () => {

    it('count is zero', () => {
      expect(getMeterCount()).toBe(0);
      expect(getMeterCount(undefined)).toBe(0);
    });
  });

  describe('given widget state', () => {

    const requestHttp: RequestsHttp = {isSuccessfullyFetched: false, isFetching: false};
    const state: WidgetState = {4: {id: 4, data: 99, ...requestHttp}};

    it('count is zero when id cannot be found', () => {
      const emptyState: WidgetState = {4: {id: 4, data: undefined, ...requestHttp}};

      expect(getMeterCount(emptyState[1])).toBe(0);
    });

    it('finds meter with id', () => {
      expect(getMeterCount(state[4])).toBe(99);
    });

    it('is not fetching when id cannot be found', () => {
      expect(isFetching(state[1])).toBe(false);
    });

    it('is not fetching by default', () => {
      expect(isFetching(state[4])).toBe(false);
    });

    it('is fetching ', () => {
      const fetchingState = {4: {id: 4, data: 99, ...requestHttp, isFetching: true}};

      expect(isFetching(fetchingState[4])).toBe(true);
    });

  });
});

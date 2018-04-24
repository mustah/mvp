import {Pathname} from 'history';
import {RouterState} from 'react-router-redux';
import {getPathname, isReportPage, isSelectionPage} from '../routerSelectors';

describe('routerSelectors', () => {

  interface State {
    location: {pathname: Pathname} | null;
  }

  describe('selectLocation', () => {
    it('will always have a location when getting pathname', () => {
      expect(getPathname(stateWith('/home'))).toEqual('/home');
    });

    it('throws when unwrapping a null location', () => {
      expect(() => getPathname({location: null})).toThrow();
    });
  });

  describe('isSelectionPage', () => {

    it('is not selection page', () => {
      expect(isSelectionPage(stateWith('selection'))).toBe(false);
      expect(isSelectionPage(stateWith('/ selection'))).toBe(false);
      expect(isSelectionPage(stateWith('/selection  '))).toBe(false);
      expect(isSelectionPage(stateWith('/home'))).toBe(false);
    });

    it('is selection page when matching exactly', () => {
      expect(isSelectionPage(stateWith('/selection'))).toBe(true);
    });
  });

  describe('isReportPage', () => {

    it('is not report page', () => {
      expect(isReportPage(stateWith('report'))).toBe(false);
      expect(isReportPage(stateWith('/ report'))).toBe(false);
      expect(isReportPage(stateWith('/report  '))).toBe(false);
      expect(isReportPage(stateWith('/home'))).toBe(false);
    });

    it('is report page when matching exactly', () => {
      expect(isReportPage(stateWith('/report'))).toBe(true);
    });
  });

  const stateWith = (pathname: Pathname): RouterState => {
    const state: State = {location: {pathname}};
    return state as RouterState;
  };
});

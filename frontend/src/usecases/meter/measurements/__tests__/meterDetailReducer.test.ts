import {Location} from 'history';
import {routes} from '../../../../app/routes';
import {Period, TemporalResolution} from '../../../../components/dates/dateModels';
import {locationChange} from '../../../../state/location/locationActions';
import {SelectionInterval} from '../../../../state/user-selection/userSelectionModels';
import {selectResolution, setTimePeriod} from '../meterDetailActions';
import {MeterDetailState} from '../meterDetailModels';
import {initialState, meterDetail} from '../meterDetailReducer';

describe('meterDetailReducer', () => {

  describe('location change', () => {

    it('does not reset state when location is on meter page', () => {
      const location = makeLocation(`${routes.meter}/${'123'}`);

      const state: MeterDetailState = {...initialState, isDirty: true};

      const newState: MeterDetailState = meterDetail(state, locationChange(location));

      expect(newState).toBe(state);
    });

    it('resets state when location is not on meter details page', () => {
      const location = makeLocation(`${routes.meters}`);

      const state: MeterDetailState = {...initialState, isDirty: true};

      const newState: MeterDetailState = meterDetail(state, locationChange(location));

      expect(newState).toEqual(initialState);
    });

    const makeLocation = (pathname: string): Location => ({
      pathname,
      search: '',
      state: {},
      hash: '',
    });
  });

  describe('setTimePeriod', () => {

    it('marks state as dirty', () => {
      const timePeriod: SelectionInterval = {period: Period.currentWeek};

      const state: MeterDetailState = meterDetail(initialState, setTimePeriod(timePeriod));

      const expected: MeterDetailState = {...initialState, timePeriod, isDirty: true};
      expect(state).toEqual(expected);
    });
  });

  describe('setResolution', () => {

    it('marks state as dirty', () => {
      const resolution = TemporalResolution.month;
      const state: MeterDetailState = meterDetail(initialState, selectResolution(resolution));

      const expected: MeterDetailState = {...initialState, resolution, isDirty: true};
      expect(state).toEqual(expected);
    });

    it('has state marked as dirty when changing between resolution and period', () => {
      const resolution = TemporalResolution.month;

      const state: MeterDetailState = meterDetail(initialState, selectResolution(resolution));

      const timePeriod: SelectionInterval = {period: Period.currentWeek};

      const newState: MeterDetailState = meterDetail(state, setTimePeriod(timePeriod));

      const expected: MeterDetailState = {...initialState, resolution, timePeriod, isDirty: true};
      expect(newState).toEqual(expected);
    });
  });

});

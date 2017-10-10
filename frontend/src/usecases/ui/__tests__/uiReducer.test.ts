import {selectIndicatorWidget} from '../../common/components/indicators/indicatorActions';
import {indicator, IndicatorState, initialState} from '../../common/components/indicators/indicatorReducer';
import {IndicatorType} from '../../common/components/indicators/models/IndicatorModels';

describe('uiReducer', () => {

  describe('indicators -> selectedIndicators', () => {

    it('will have initial state when the action is not matched', () => {
      const state: IndicatorState = indicator(initialState, {type: 'UNKNOWN_ACTION'});

      expect(state).toBe(initialState);
    });

    it('will have dashboard indicator state after initial state', () => {
      const action = selectIndicatorWidget({dashboard: IndicatorType.collection});
      const state: IndicatorState = indicator(initialState, action);

      const selectedIndicators = {
        dashboard: IndicatorType.collection,
        report: null,
      };

      expect(state).toEqual({selectedIndicators});
    });

    it('will have report indicator state after initial state', () => {
      const action = selectIndicatorWidget({report: IndicatorType.districtHeating});
      const state: IndicatorState = indicator(initialState, action);

      const selectedIndicators = {
        dashboard: null,
        report: IndicatorType.districtHeating,
      };

      expect(state).toEqual({selectedIndicators});
    });

    it('updates indicator state after many action dispatches', () => {
      const oldState: IndicatorState =
        indicator(initialState, selectIndicatorWidget({report: IndicatorType.districtHeating}));
      const state: IndicatorState =
        indicator(oldState, selectIndicatorWidget({dashboard: IndicatorType.measurementQuality}));

      const selectedIndicators = {
        dashboard: IndicatorType.measurementQuality,
        report: IndicatorType.districtHeating,
      };

      expect(state).toEqual({selectedIndicators});
    });

    it('clears any previous selections', () => {
      const oldState: IndicatorState =
        indicator(initialState, selectIndicatorWidget({report: IndicatorType.districtHeating}));
      const state: IndicatorState =
        indicator(oldState, selectIndicatorWidget({report: null}));

      const selectedIndicators = {
        dashboard: null,
        report: null,
      };

      expect(state).toEqual({selectedIndicators});
    });

  });
});

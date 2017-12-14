import {IndicatorType} from '../../../components/indicators/indicatorWidgetModels';
import {selectIndicatorWidget} from '../indicator/indicatorActions';
import {indicator, IndicatorState, initialState} from '../indicator/indicatorReducer';

describe('uiReducer', () => {

  describe('indicators -> selectedIndicators', () => {

    it('will have dashboard indicator state after initial state', () => {
      const action = selectIndicatorWidget({dashboard: IndicatorType.collection});
      const state: IndicatorState = indicator(initialState, action);

      const selectedIndicators = {
        dashboard: IndicatorType.collection,
      };

      expect(state).toEqual({selectedIndicators});
    });

    it('will have report indicator state after initial state', () => {
      const action = selectIndicatorWidget({report: IndicatorType.districtHeating});
      const state: IndicatorState = indicator(initialState, action);

      const selectedIndicators = {
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

  });
});

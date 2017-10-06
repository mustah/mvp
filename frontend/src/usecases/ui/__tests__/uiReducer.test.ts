import {IndicatorType} from '../../common/components/indicators/models/IndicatorModels';
import {selectIndicatorWidget} from '../uiActions';
import {initialState, ui, UiState} from '../uiReducer';

describe('uiReducer', () => {

  describe('selectedIndicators', () => {

    it('will have initial state when the action is not matched', () => {
      const state: UiState = ui(initialState, {type: 'UNKNOWN_ACTION'});

      expect(state).toBe(initialState);
    });

    it('will have dashboard ui state after initial state', () => {
      const action = selectIndicatorWidget({dashboard: IndicatorType.collection});
      const state: UiState = ui(initialState, action);

      const selectedIndicators = {
        dashboard: IndicatorType.collection,
        report: null,
      };

      expect(state).toEqual({selectedIndicators});
    });

    it('will have report ui state after initial state', () => {
      const action = selectIndicatorWidget({report: IndicatorType.districtHeating});
      const state: UiState = ui(initialState, action);

      const selectedIndicators = {
        dashboard: null,
        report: IndicatorType.districtHeating,
      };

      expect(state).toEqual({selectedIndicators});
    });

    it('updates ui state after many action dispatches', () => {
      const oldState: UiState = ui(initialState, selectIndicatorWidget({report: IndicatorType.districtHeating}));
      const state: UiState = ui(oldState, selectIndicatorWidget({dashboard: IndicatorType.measurementQuality}));

      const selectedIndicators = {
        dashboard: IndicatorType.measurementQuality,
        report: IndicatorType.districtHeating,
      };

      expect(state).toEqual({selectedIndicators});
    });

    it('clears any previous selections', () => {
      const oldState: UiState = ui(initialState, selectIndicatorWidget({report: IndicatorType.districtHeating}));
      const state: UiState = ui(oldState, selectIndicatorWidget({report: null}));

      const selectedIndicators = {
        dashboard: null,
        report: null,
      };

      expect(state).toEqual({selectedIndicators});
    });

  });
});

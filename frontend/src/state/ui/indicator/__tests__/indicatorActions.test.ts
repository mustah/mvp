import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {Medium} from '../../../../components/indicators/indicatorWidgetModels';
import {Quantity} from '../../graph/measurement/measurementModels';
import {
  canToggleMedia,
  selectQuantities,
  setReportIndicatorWidgets,
  toggleReportIndicatorWidget,
} from '../indicatorActions';
import {IndicatorState, initialState} from '../indicatorReducer';

describe('indicatorActions', () => {

  const configureMockStore = configureStore([thunk]);

  const storeWith = (state: IndicatorState) =>
    configureMockStore({
      ui: {
        indicator: {
          ...state,
        },
      },
    });

  describe('toggleReportIndicatorWidget', () => {

    it('can select an indicator', () => {
      const store = storeWith(initialState);

      store.dispatch(toggleReportIndicatorWidget(Medium.gas));

      const expected = [
        setReportIndicatorWidgets([Medium.gas]),
        selectQuantities([Quantity.volume]),
      ];

      expect(store.getActions()).toEqual(expected);
    });

    it('can have multiple indicators selected at the same time', () => {
      const store = storeWith({
        selectedIndicators: {
          report: [Medium.districtHeating],
        },
        selectedQuantities: [
          Quantity.volume,
        ],
      });

      store.dispatch(toggleReportIndicatorWidget(Medium.gas));

      const expected = [
        setReportIndicatorWidgets([Medium.districtHeating, Medium.gas]),
      ];

      expect(store.getActions()).toEqual(expected);
    });

    it('can deselect indicators', () => {
      const store = storeWith({
        selectedIndicators: {
          report: [Medium.districtHeating],
        },
        selectedQuantities: [],
      });

      store.dispatch(toggleReportIndicatorWidget(Medium.districtHeating));

      const expected = [
        setReportIndicatorWidgets([]),
      ];

      expect(store.getActions()).toEqual(expected);
    });

    describe('defaults to certain quantity', () => {

      it('defaults to energy for district heating', () => {
        const store = storeWith(initialState);

        store.dispatch(toggleReportIndicatorWidget(Medium.districtHeating));

        const expected = [
          setReportIndicatorWidgets([Medium.districtHeating]),
          selectQuantities([Quantity.energy]),
        ];

        expect(store.getActions()).toEqual(expected);
      });

      it('defaults to volume for gas', () => {
        const store = storeWith(initialState);

        store.dispatch(toggleReportIndicatorWidget(Medium.gas));

        const expected = [
          setReportIndicatorWidgets([Medium.gas]),
          selectQuantities([Quantity.volume]),
        ];

        expect(store.getActions()).toEqual(expected);
      });

      it('adds default quantities for second media if we do not have extra quantities selected', () => {
        const store = storeWith({
          selectedIndicators: {
            report: [Medium.gas],
          },
          selectedQuantities: [
            Quantity.volume,
          ],
        });

        store.dispatch(toggleReportIndicatorWidget(Medium.districtHeating));

        const expected = [
          setReportIndicatorWidgets([Medium.gas, Medium.districtHeating]),
          selectQuantities([Quantity.volume, Quantity.energy]),
        ];

        expect(store.getActions()).toEqual(expected);
      });

      it('does not select default quantity if a third media is selected', () => {
        const store = storeWith({
          selectedIndicators: {
            report: [Medium.gas, Medium.districtHeating],
          },
          selectedQuantities: [
            Quantity.volume,
            Quantity.energy,
          ],
        });

        store.dispatch(toggleReportIndicatorWidget(Medium.roomSensor));

        const expected = [
          setReportIndicatorWidgets([Medium.gas, Medium.districtHeating, Medium.roomSensor]),
        ];

        expect(store.getActions()).toEqual(expected);
      });

    });

  });

  describe('canToggleMedia', () => {

    it('allows medium to be added if none is selected', () => {
      const allowed: boolean = canToggleMedia([], Quantity.energy);
      expect(allowed).toBeTruthy();
    });

    it('allows medium to be added if only one quantity is selected', () => {
      const allowed: boolean = canToggleMedia([], Quantity.energy);
      expect(allowed).toBeTruthy();
    });

    it('allows many quantities to be added, as long as they represent at most two unique units', () => {
      const sharedUnit: boolean = canToggleMedia(
        [Quantity.returnTemperature, Quantity.power],
        Quantity.forwardTemperature,
      );
      expect(sharedUnit).toBeTruthy();
    });

    it('does not allow adding a third different unit', () => {
      const threeUnits: boolean = canToggleMedia(
        [Quantity.returnTemperature, Quantity.power],
        Quantity.energy,
      );
      expect(threeUnits).toBeFalsy();
    });

    it('allows for a selected medium to be toggled', () => {
      const threeUnits: boolean = canToggleMedia(
        [Quantity.returnTemperature, Quantity.power],
        Quantity.returnTemperature,
      );
      expect(threeUnits).toBeTruthy();
    });

  });

});

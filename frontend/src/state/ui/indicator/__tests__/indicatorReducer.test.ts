import {logoutUser} from '../../../../usecases/auth/authActions';
import {removeSelectedListItems, setSelectedItems} from '../../../../usecases/report/reportActions';
import {RESET_SELECTION, SELECT_SAVED_SELECTION, SET_THRESHOLD} from '../../../user-selection/userSelectionActions';
import {RelationalOperator, ThresholdQuery, UserSelection} from '../../../user-selection/userSelectionModels';
import {initialState as initialUserSelectionState} from '../../../user-selection/userSelectionReducer';
import {Medium, Quantity, quantityAttributes} from '../../graph/measurement/measurementModels';
import {indicator, IndicatorState, initialState} from '../indicatorReducer';

describe('indicatorReducer', () => {

  const state: IndicatorState = {
    ...initialState,
    selectedQuantities: [Quantity.volume],
  };

  it('deselects selected indicators and quantities when the last report item is deselected', () => {
    const newState: IndicatorState = indicator(state, setSelectedItems({
      items: [],
      media: [],
      quantities: [],
    }));

    expect(newState).toEqual(initialState);
  });

  it('selects indicator and quantity on demand', () => {
    const newState: IndicatorState = indicator(
      initialState,
      setSelectedItems({
        items: [],
        media: [Medium.roomSensor],
        quantities: [Quantity.externalTemperature],
      }),
    );

    const expected: IndicatorState = {
      ...initialState,
      selectedQuantities: [Quantity.externalTemperature],
    };

    expect(newState).toEqual(expected);
  });

  describe('reset state', () => {

    it('reset state when user selection is cleared', () => {
      const payload: UserSelection = initialUserSelectionState.userSelection;
      const newState: IndicatorState = indicator(state, {type: RESET_SELECTION, payload});

      expect(newState).toEqual(initialState);
    });

    it('reset state when another saved selection is selected', () => {
      const payload: UserSelection = initialUserSelectionState.userSelection;
      const newState: IndicatorState = indicator(state, {type: SELECT_SAVED_SELECTION, payload});

      expect(newState).toEqual(initialState);
    });

    it('resets state when user is logged out', () => {
      const newState: IndicatorState = indicator(state, logoutUser(undefined));

      expect(newState).toEqual(initialState);
    });
  });

  describe('setThreshold', () => {

    it('selects medium and quantities from threshold', () => {
      const payload: ThresholdQuery = {
        quantity: Quantity.returnTemperature,
        relationalOperator: RelationalOperator.lt,
        unit: quantityAttributes[Quantity.returnTemperature].unit,
        value: '0',
      };
      const newState: IndicatorState = indicator(state, {type: SET_THRESHOLD, payload});
      const expected: IndicatorState = {
        ...initialState,
        selectedQuantities: [Quantity.returnTemperature],
      };
      expect(newState).toEqual(expected);
    });
  });

  describe('removeSelectedListItems', () => {

    it('removes all selected indicators', () => {
      const newState: IndicatorState = indicator(state, removeSelectedListItems());
      expect(newState).toEqual(initialState);
    });
  });
});

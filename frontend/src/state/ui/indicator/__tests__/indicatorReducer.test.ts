import {logoutUser} from '../../../../usecases/auth/authActions';
import {setSelectedEntries} from '../../../../usecases/report/reportActions';
import {RESET_SELECTION, SELECT_SAVED_SELECTION} from '../../../user-selection/userSelectionActions';
import {UserSelection} from '../../../user-selection/userSelectionModels';
import {initialState as initialUserSelectionState} from '../../../user-selection/userSelectionReducer';
import {Medium, Quantity} from '../../graph/measurement/measurementModels';
import {indicator, IndicatorState, initialState} from '../indicatorReducer';

describe('indicatorReducer', () => {

  const state: IndicatorState = {
    ...initialState,
    selectedIndicators: {
      report: [Medium.districtHeating],
    },
    selectedQuantities: [Quantity.volume],
  };

  it('deselects selected indicators and quantities when the last report item is deselected', () => {
    const newState: IndicatorState = indicator(state, setSelectedEntries({
      ids: [],
      indicatorsToSelect: [],
      quantitiesToSelect: [],
    }));

    expect(newState).toEqual(initialState);
  });

  it('selects indicator and quantity on demand', () => {
    const newState: IndicatorState = indicator(
      initialState,
      setSelectedEntries({
        ids: ['123'],
        indicatorsToSelect: [Medium.roomSensor],
        quantitiesToSelect: [Quantity.externalTemperature],
      }),
    );

    const expected: IndicatorState = {
      ...initialState,
      selectedIndicators: {
        report: [Medium.roomSensor],
      },
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

});

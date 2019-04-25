import {resetSelection, selectSavedSelectionAction} from '../../../state/user-selection/userSelectionActions';
import {initialState as initialUserSelection} from '../../../state/user-selection/userSelectionReducer';
import {logoutUser} from '../../auth/authActions';
import {onCenterMap} from '../mapActions';
import {MapZoomSettings, MapZoomSettingsPayload} from '../mapModels';
import {initialState, map, MapState} from '../mapReducer';

describe('mapReducer', () => {

  const zoomSettings: MapZoomSettings = {center: {latitude: 1, longitude: 2}, zoom: 8};

  it('centers on map', () => {
    const centerPayload: MapZoomSettingsPayload = {id: 1, ...zoomSettings};

    const state: MapState = map(initialState, onCenterMap(centerPayload));

    const expected: MapState = {1: {...zoomSettings}};

    expect(state).toEqual(expected);
  });

  it('updates map settings by id', () => {
    const prevState: MapState = {
      1: {...zoomSettings},
      2: {...zoomSettings},
    };

    const state: MapState = map(prevState, onCenterMap({id: 2, ...zoomSettings, zoom: 13}));

    const expected: MapState = {
      1: {...zoomSettings},
      2: {...zoomSettings, zoom: 13},
    };
    expect(state).toEqual(expected);
  });

  describe('selectSavedSelectionAction', () => {

    it('resets zoom settings when saved selection is selected', () => {
      const prevState: MapState = {
        1: {...zoomSettings},
        2: {...zoomSettings},
      };

      const state: MapState = map(prevState, selectSavedSelectionAction(initialUserSelection.userSelection));

      expect(state).toEqual(initialState);
    });

    it('resets zoom settings when selection named All is selected', () => {
      const prevState: MapState = {
        1: {...zoomSettings},
        2: {...zoomSettings},
      };

      const state: MapState = map(prevState, resetSelection());

      expect(state).toEqual(initialState);
    });
  });

  describe('logout user', () => {

    it('resets state to initial state', () => {
      const state = map({8: {...zoomSettings}}, logoutUser(undefined));

      expect(state).toEqual(initialState);
    });
  });
});

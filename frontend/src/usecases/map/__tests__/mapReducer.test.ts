import {logoutUser} from '../../auth/authActions';
import {centerMap, closeClusterDialog, openDialog} from '../mapActions';
import {initialState, map, MapState} from '../mapReducer';

describe('mapReducer', () => {

  it('closes cluster dialog', () => {
    const state: MapState = map(initialState, closeClusterDialog());
    expect(state).toEqual({isClusterDialogOpen: false});
  });

  it('opens cluster dialog for marker with id', () => {
    const state: MapState = map(initialState, openDialog(1));
    expect(state).toEqual({isClusterDialogOpen: true, selectedMarker: 1});
  });

  it('centers on map', () => {
    expect(initialState.viewCenter).toBeUndefined();

    const geoPosition = {latitude: 1, longitude: 2};
    const state: MapState = map(initialState, centerMap(geoPosition));

    expect(state).toEqual({...initialState, viewCenter: geoPosition});
  });

  describe('logout user', () => {

    it('resets state to initial state', () => {
      let state: MapState = {isClusterDialogOpen: true, selectedMarker: 1};

      state = map(state, logoutUser(undefined));

      expect(state).toEqual({isClusterDialogOpen: false});
    });
  });
});

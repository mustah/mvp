import {LOGOUT_USER} from '../../auth/authActions';
import {CLOSE_CLUSTER_DIALOG, OPEN_CLUSTER_DIALOG} from '../mapActions';
import {initialState, map, MapState} from '../mapReducer';

describe('mapReducer', () => {

  it('should handle CLOSE_CLUSTER_DIALOG', () => {
    const state: MapState = map(initialState, {type: CLOSE_CLUSTER_DIALOG, payload: {}});
    expect(state).toEqual({isClusterDialogOpen: false});
  });

  it('should handle OPEN_CLUSTER_DIALOG', () => {
    const state: MapState = map(initialState, {type: OPEN_CLUSTER_DIALOG, payload: {}});
    expect(state).toEqual({isClusterDialogOpen: true, selectedMarker: {}});
  });

  describe('logout user', () => {

    it('resets state to initial state', () => {
      let state: MapState = {isClusterDialogOpen: true, selectedMarker: 1};

      state = map(state, {type: LOGOUT_USER});

      expect(state).toEqual({isClusterDialogOpen: false});
    });
  });
});

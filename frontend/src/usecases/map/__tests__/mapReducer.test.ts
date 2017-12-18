import {initialState, map, MapState} from '../mapReducer';

describe('mapReducer', () => {

  it('should handle CLOSE_CLUSTER_DIALOG', () => {
    const state: MapState = map(initialState, {type: 'CLOSE_CLUSTER_DIALOG', payload: {}});
    expect(state).toEqual({isClusterDialogOpen: false});
  });

  it('should handle OPEN_CLUSTER_DIALOG', () => {
    const state: MapState = map(initialState, {type: 'OPEN_CLUSTER_DIALOG', payload: {}});
    expect(state).toEqual({isClusterDialogOpen: true, selectedMarker: {}});
  });
});

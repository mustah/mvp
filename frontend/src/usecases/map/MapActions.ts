import {createEmptyAction, createPayloadAction} from 'react-redux-typescript';
import {Marker} from 'leaflet';
import {restClient} from '../../services/restClient';
import {MapState} from './MapReducer';

export const TOGGLE_CLUSTER_DIALOG = 'TOGGLE_CLUSTER_DIALOG';
export const OPEN_CLUSTER_DIALOG = 'OPEN_CLUSTER_DIALOG';
export const MAP_POSITION_REQUEST = 'MAP_POSITION_REQUEST';
export const MAP_POSITION_SUCCESS = 'MAP_POSITION_SUCCESS';
export const MAP_POSITION_FAILURE = 'MAP_POSITION_FAILURE';

export const toggleClusterDialog = createEmptyAction<string>(TOGGLE_CLUSTER_DIALOG);
export const mapPositionSuccess = createPayloadAction<string, MapState>(MAP_POSITION_SUCCESS);
export const mapPositionFailure = createPayloadAction<string, MapState>(MAP_POSITION_FAILURE);

export const openClusterDialog = (marker: Marker) => {
  return (dispatch) => dispatch(createPayloadAction(OPEN_CLUSTER_DIALOG) (marker));
};

export const fetchPositions = () => {
    return (dispatch) => {
      dispatch(createEmptyAction(MAP_POSITION_REQUEST));

      restClient.get('/meters')
        .then(response => response.data)
        .then(positions => dispatch(mapPositionSuccess(positions)))
        .catch(error => dispatch(mapPositionFailure(error)));
    };
};

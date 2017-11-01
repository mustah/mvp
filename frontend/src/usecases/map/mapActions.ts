import {Marker} from 'leaflet';
import {createEmptyAction, createPayloadAction} from 'react-redux-typescript';
import {restClient} from '../../services/restClient';
import {makeUrl} from '../../services/urlFactory';
import {MapState} from './mapReducer';

export const TOGGLE_CLUSTER_DIALOG = 'TOGGLE_CLUSTER_DIALOG';
export const OPEN_CLUSTER_DIALOG = 'OPEN_CLUSTER_DIALOG';
export const MAP_POSITION_REQUEST = 'MAP_POSITION_REQUEST';
export const MAP_POSITION_SUCCESS = 'MAP_POSITION_SUCCESS';
export const MAP_POSITION_FAILURE = 'MAP_POSITION_FAILURE';

export const toggleClusterDialog = createEmptyAction<string>(TOGGLE_CLUSTER_DIALOG);
export const openDialog = createPayloadAction<string, Marker>(OPEN_CLUSTER_DIALOG);

const mapPositionRequest = createEmptyAction<string>(MAP_POSITION_REQUEST);
const mapPositionSuccess = createPayloadAction<string, MapState>(MAP_POSITION_SUCCESS);
const mapPositionFailure = createPayloadAction<string, MapState>(MAP_POSITION_FAILURE);

export const openClusterDialog = (marker: Marker) => (dispatch) => dispatch(openDialog(marker));

export const fetchPositions = (encodedUriParameters: string) =>
  async (dispatch) => {
    try {
      dispatch(mapPositionRequest());
      const {data: meters} = await restClient.get(makeUrl('/meters', encodedUriParameters));
      dispatch(mapPositionSuccess(meters));
    } catch (error) {
      const {response: {data}} = error;
      dispatch(mapPositionFailure(data));
    }
  };

import {normalize} from 'normalizr';
import {createEmptyAction, createPayloadAction} from 'react-redux-typescript';
import {restClient} from '../../../services/restClient';
import {ErrorResponse, IdNamed} from '../../../types/Types';
import {Normalized} from './geoDataModels';
import {geoDataSchema, sidebarTreeSchema} from './geoDataSchemas';

export const GEO_DATA_REQUEST = 'GEO_DATA_REQUEST';
export const GEO_DATA_SUCCESS = 'GEO_DATA_SUCCESS';
export const GEO_DATA_FAILURE = 'GEO_DATA_FAILURE';

export const geoDataRequest = createEmptyAction(GEO_DATA_REQUEST);
export const geoDataSuccess = createPayloadAction<string, Normalized<IdNamed>>(GEO_DATA_SUCCESS);
export const geoDataFailure = createPayloadAction<string, ErrorResponse>(GEO_DATA_FAILURE);

export const fetchGeoData = () =>
  async (dispatch) => {
    try {
      dispatch(geoDataRequest());
      const {data: geoData} = await restClient.get('/selections');
      dispatch(geoDataSuccess(normalize(geoData, geoDataSchema)));
    } catch (error) {
      const {response: {data}} = error;
      dispatch(geoDataFailure(data));
    }
  };

export const SIDEBAR_TREE_REQUEST = 'SIDEBAR_TREE_REQUEST';
export const SIDEBAR_TREE_SUCCESS = 'SIDEBAR_TREE_SUCCESS';
export const SIDEBAR_TREE_FAILURE = 'SIDEBAR_TREE_FAILURE';

export const sidebarTreeRequest = createEmptyAction(SIDEBAR_TREE_REQUEST);
export const sidebarTreeSuccess = createPayloadAction(SIDEBAR_TREE_SUCCESS);
export const sidebarTreeFailure = createPayloadAction(SIDEBAR_TREE_FAILURE);

export const fetchSidebarTreeData = () =>
  async (dispatch) => {
    try {
      dispatch(sidebarTreeRequest());
      const {data: sidebarTreeData} = await restClient.get('/sidebarTree');
      dispatch(sidebarTreeSuccess(normalize(sidebarTreeData, sidebarTreeSchema)));
    } catch (error) {
      const {response: {data}} = error;
      dispatch(sidebarTreeFailure(data));
    }
  };

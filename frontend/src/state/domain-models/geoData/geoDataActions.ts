import {normalize} from 'normalizr';
import {createEmptyAction, createPayloadAction} from 'react-redux-typescript';
import {restClient} from '../../../services/restClient';
import {ErrorResponse, IdNamed} from '../../../types/Types';
import {Normalized} from './geoDataModels';
import {geoDataSchema} from './geoDataSchemas';

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

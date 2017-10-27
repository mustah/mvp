import {normalize} from 'normalizr';
import {createEmptyAction, createPayloadAction} from 'react-redux-typescript';
import {filterToUri, restClient} from '../../../services/restClient';
import {meterSchema} from './meterSchema';

export const METER_REQUEST = 'METER_REQUEST';
export const METER_SUCCESS = 'METER_SUCCESS';
export const METER_FAILURE = 'METER_FAILURE';

const meterRequest = createEmptyAction(METER_REQUEST);
const meterSuccess = createPayloadAction(METER_SUCCESS);
const meterFailure = createPayloadAction(METER_FAILURE);

export const fetchMeters = (filter) => {
  return (dispatch) => {
    dispatch(meterRequest());

    const parameters = {
      ...filter,
    };

    restClient.get(filterToUri('/meters', parameters))
      .then(response => {
        const meters = response.data;
        return dispatch(meterSuccess({
          meters: normalize(meters, meterSchema),
        }));
      })
      .catch(error => dispatch(meterFailure(error)));
  };
};

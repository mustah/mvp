import {normalize} from 'normalizr';
import {createEmptyAction, createPayloadAction} from 'react-redux-typescript';
import {Dispatch} from 'redux';
import {restClient} from '../../../services/restClient';
import {makeUrl} from '../../../services/urlFactory';
import {meterSchema} from './meterSchema';

export const METER_REQUEST = 'METER_REQUEST';
export const METER_SUCCESS = 'METER_SUCCESS';
export const METER_FAILURE = 'METER_FAILURE';

export const meterRequest = createEmptyAction(METER_REQUEST);
export const meterSuccess = createPayloadAction(METER_SUCCESS);
export const meterFailure = createPayloadAction(METER_FAILURE);

export const fetchMeters = (encodedUriParameters: string) =>
  async (dispatch: Dispatch<any>) => {
    try {
      dispatch(meterRequest());
      const {data: meters} = await restClient.get(makeUrl('/meters', encodedUriParameters));
      dispatch(meterSuccess({meters: normalize(meters, meterSchema)}));
    } catch (error) {
      const {response: {data}} = error;
      dispatch(meterFailure(data));
    }
  };

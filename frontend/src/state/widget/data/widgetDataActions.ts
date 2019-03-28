import {createStandardAction} from 'typesafe-actions';
import {InvalidToken} from '../../../exceptions/InvalidToken';
import {GetState} from '../../../reducers/rootReducer';
import {EndPoints} from '../../../services/endPoints';
import {isTimeoutError, restClient, wasRequestCanceled} from '../../../services/restClient';
import {EncodedUriParameters, ErrorResponse, Identifiable, uuid} from '../../../types/Types';
import {logout} from '../../../usecases/auth/authActions';
import {meterMapMarkersDataFormatter} from '../../../usecases/map/mapMarkerSchema';
import {noInternetConnection, requestTimeout, responseMessageOrFallback} from '../../api/apiActions';
import {shouldFetch} from '../../domain-models/domainModelsActions';
import {WidgetSettings} from '../configuration/widgetConfigurationReducer';
import {WidgetData} from './widgetDataReducer';

interface WidgetRequest {
  requestFactory: (requestArguments: FetchWidgetIfNeeded) => Promise<WidgetData>;
}

export interface FetchWidgetIfNeeded {
  settings: WidgetSettings;
  parameters: EncodedUriParameters;
}

export const widgetDataActions = {
  request: createStandardAction('WIDGET_DATA_REQUEST')<uuid>(),
  success: createStandardAction('WIDGET_DATA_SUCCESS')<WidgetData>(),
  failure: createStandardAction('WIDGET_DATA_FAILURE')<ErrorResponse & Identifiable>(),
};

const fetchWidgetIfNeeded = ({requestFactory}: WidgetRequest) =>
  (requestArguments: FetchWidgetIfNeeded) =>
    async (dispatch, getState: GetState) => {
      const {widget: {data}} = getState();
      const {settings: {id}} = requestArguments;
      if (!data[id] || shouldFetch(data[id])) {
        try {
          dispatch(widgetDataActions.request(id));
          const formattedResponse = await requestFactory(requestArguments);
          dispatch(widgetDataActions.success(formattedResponse));
        } catch (error) {
          if (error instanceof InvalidToken) {
            await dispatch(logout(error));
          } else if (wasRequestCanceled(error)) {
            return;
          } else if (isTimeoutError(error)) {
            dispatch(widgetDataActions.failure({...requestTimeout(), id}));
          } else if (!error.response) {
            dispatch(widgetDataActions.failure({...noInternetConnection(), id}));
          } else {
            dispatch(widgetDataActions.failure({...responseMessageOrFallback(error.response), id}));
          }
        }
      }
    };

export const fetchCollectionStatsWidget =
  fetchWidgetIfNeeded({
    requestFactory: async ({settings: {settings, id}, parameters}: FetchWidgetIfNeeded) => {
      // TODO if every URL is unique, `getForced()` is bad and we really want the old, "matching url" behavior
      // TODO contd. maybe we could try putting the URL in the `isFetching` field instead...
      const response = await restClient.getForced(
        `${EndPoints.collectionStatDate}/?${parameters}`
      );
      return {
        id,
        data: response.data.reduce(
          (all, current) => (all + current.collectionPercentage),
          0
        ) / response.data.length
      };
    },
  });

export const fetchMapWidget =
  fetchWidgetIfNeeded({
    requestFactory: async ({settings: {settings, id}, parameters}: FetchWidgetIfNeeded) => {
      // TODO if every URL is unique, `getForced()` is bad and we really want the old, "matching url" behavior
      // TODO contd. maybe we could try putting the URL in the `isFetching` field instead...

      const response = await restClient.getForced(`${EndPoints.meterMapMarkers}/?${parameters}`);
      const data = meterMapMarkersDataFormatter(response.data);
      return {
        id,
        data,
      };
    },
  });

export const fetchCountWidget =
  fetchWidgetIfNeeded({
    requestFactory: async ({settings: {id}, parameters}: FetchWidgetIfNeeded) => {
      const response = await restClient.getForced(`${EndPoints.summaryMeters}/?${parameters}`);
      const data = response.data;
      return {
        id,
        data,
      };
    },
  });

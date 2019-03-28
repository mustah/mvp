import {createStandardAction} from 'typesafe-actions';
import {InvalidToken} from '../../exceptions/InvalidToken';
import {GetState} from '../../reducers/rootReducer';
import {EndPoints} from '../../services/endPoints';
import {isTimeoutError, restClient, wasRequestCanceled} from '../../services/restClient';
import {EncodedUriParameters, ErrorResponse, Identifiable, uuid} from '../../types/Types';
import {logout} from '../../usecases/auth/authActions';
import {meterMapMarkersDataFormatter} from '../../usecases/map/mapMarkerSchema';
import {noInternetConnection, requestTimeout, responseMessageOrFallback} from '../api/apiActions';
import {shouldFetch} from '../domain-models/domainModelsActions';
import {Widget} from '../domain-models/widget/widgetModels';
import {WidgetData} from './widgetReducer';

interface WidgetRequest {
  requestFactory: (requestArguments: WidgetRequestParameters) => Promise<WidgetData>;
}

export interface WidgetRequestParameters {
  widget: Widget;
  parameters: EncodedUriParameters;
}

export const widgetActions = {
  request: createStandardAction('WIDGET_DATA_REQUEST')<uuid>(),
  success: createStandardAction('WIDGET_DATA_SUCCESS')<WidgetData>(),
  failure: createStandardAction('WIDGET_DATA_FAILURE')<ErrorResponse & Identifiable>(),
};

const fetchWidgetIfNeeded = ({requestFactory}: WidgetRequest) =>
  (requestParameters: WidgetRequestParameters) =>
    async (dispatch, getState: GetState) => {
      const {widget} = getState();
      const {widget: {id}} = requestParameters;
      if (!widget[id] || shouldFetch(widget[id])) {
        try {
          dispatch(widgetActions.request(id));
          const formattedResponse = await requestFactory(requestParameters);
          dispatch(widgetActions.success(formattedResponse));
        } catch (error) {
          if (error instanceof InvalidToken) {
            await dispatch(logout(error));
          } else if (wasRequestCanceled(error)) {
            return;
          } else if (isTimeoutError(error)) {
            dispatch(widgetActions.failure({...requestTimeout(), id}));
          } else if (!error.response) {
            dispatch(widgetActions.failure({...noInternetConnection(), id}));
          } else {
            dispatch(widgetActions.failure({...responseMessageOrFallback(error.response), id}));
          }
        }
      }
    };

export const fetchCollectionStatsWidget =
  fetchWidgetIfNeeded({
    requestFactory: async ({widget: {settings, id}, parameters}: WidgetRequestParameters) => {
      // TODO if every URL is unique, `getForced()` is bad and we really want the old, "matching url" behavior
      // TODO contd. maybe we could try putting the URL in the `isFetching` field instead...
      const response = await restClient.getForced(`${EndPoints.collectionStatDate}/?${parameters}`);
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
    requestFactory: async ({widget: {settings, id}, parameters}: WidgetRequestParameters) => {
      // TODO if every URL is unique, `getForced()` is bad and we really want the old, "matching url" behavior
      // TODO contd. maybe we could try putting the URL in the `isFetching` field instead...
      const response = await restClient.getForced(`${EndPoints.meterMapMarkers}/?${parameters}`);
      const data = meterMapMarkersDataFormatter(response.data);
      return {id, data};
    },
  });

export const fetchCountWidget =
  fetchWidgetIfNeeded({
    requestFactory: async ({widget: {id}, parameters}: WidgetRequestParameters) => {
      const {data} = await restClient.getForced(`${EndPoints.summaryMeters}/?${parameters}`);
      return {id, data};
    },
  });

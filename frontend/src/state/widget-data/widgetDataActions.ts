import {createStandardAction} from 'typesafe-actions';
import {InvalidToken} from '../../exceptions/InvalidToken';
import {GetState} from '../../reducers/rootReducer';
import {EndPoints} from '../../services/endPoints';
import {isTimeoutError, restClient, wasRequestCanceled} from '../../services/restClient';
import {EncodedUriParameters, ErrorResponse, Identifiable, uuid} from '../../types/Types';
import {logout} from '../../usecases/auth/authActions';
import {CollectionStatusWidgetSettings} from '../../usecases/dashboard/containers/CollectionStatusContainer';
import {noInternetConnection, requestTimeout, responseMessageOrFallback} from '../api/apiActions';
import {shouldFetch} from '../domain-models/domainModelsActions';
import {WidgetData} from '../widget/data/widgetDataReducer';

interface WidgetRequest {
  requestFactory: (requestArguments: FetchWidgetIfNeeded) => Promise<WidgetData>;
  actions: {
    request: (id: uuid) => void,
    success: (payload: WidgetData) => void,
    failure: (payload: ErrorResponse & Identifiable) => void,
  };
}

export interface FetchWidgetIfNeeded {
  settings: CollectionStatusWidgetSettings;
  parameters: EncodedUriParameters;
}

export const fetchWidgetIfNeeded = ({
  requestFactory,
  actions: {request, success, failure}
}: WidgetRequest) =>
  (requestArguments: FetchWidgetIfNeeded) =>
    async (dispatch, getState: GetState) => {
      const {widget} = getState();
      const {settings: {id}} = requestArguments;
      if (!widget[id] || shouldFetch(widget[id])) {
        try {
          dispatch(request(id));
          const formattedResponse = await requestFactory(requestArguments);
          dispatch(success(formattedResponse));
        } catch (error) {
          if (error instanceof InvalidToken) {
            await dispatch(logout(error));
          } else if (wasRequestCanceled(error)) {
            return;
          } else if (isTimeoutError(error)) {
            dispatch(failure({...requestTimeout(), id}));
          } else if (!error.response) {
            dispatch(failure({...noInternetConnection(), id}));
          } else {
            dispatch(failure({...responseMessageOrFallback(error.response), id}));
          }
        }
      }
    };

const widgetActions = <SuccessPayload extends WidgetData = WidgetData>(widgetName: string) => ({
  request: createStandardAction(`${widgetName}_WIDGET_REQUEST`)<uuid>(),
  success: createStandardAction(`${widgetName}_WIDGET_SUCCESS`)<SuccessPayload>(),
  failure: createStandardAction(`${widgetName}_WIDGET_FAILURE`)<ErrorResponse & Identifiable>(),
});

export const collectionStatWidgetActions = widgetActions('COLLECTION_STAT');

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
        // TODO do we really want a client-side mean here?
        data: response.data.reduce(
          (all, current) => (all + current.collectionPercentage),
          0
        ) / response.data.length
      };
    },
    actions: collectionStatWidgetActions,
  });

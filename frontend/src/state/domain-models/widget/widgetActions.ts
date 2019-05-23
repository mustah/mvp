import {EndPoints} from '../../../services/endPoints';
import {firstUpperTranslated} from '../../../services/translationService';
import {Dispatch, ErrorResponse} from '../../../types/Types';
import {showFailMessage, showSuccessMessage} from '../../ui/message/messageActions';
import {deleteRequest, fetchIfNeeded, postRequest, putRequest} from '../domainModelsActions';
import {Widget} from './widgetModels';
import {widgetDataFormatter} from './widgetSchema';

export const fetchWidgets = fetchIfNeeded<Widget>(
  EndPoints.widgets,
  'widgets',
  widgetDataFormatter,
);

export const addWidgetToDashboard = postRequest<Widget>(EndPoints.widgets, {
  afterFailure: ({message}: ErrorResponse, dispatch: Dispatch) => {
    dispatch(showFailMessage(firstUpperTranslated(
      'failed to add widget: {{error}}',
      {error: firstUpperTranslated(message.toLowerCase())},
    )));
  },
});

export const updateWidget = putRequest<Widget, Widget>(EndPoints.widgets, {
  afterFailure: ({message}: ErrorResponse, dispatch: Dispatch) => {
    dispatch(showFailMessage(firstUpperTranslated(
      'failed to update widget: {{error}}',
      {error: firstUpperTranslated(message.toLowerCase())},
    )));
  },
});

export const deleteWidget = deleteRequest<Widget>(EndPoints.widgets, {
    afterSuccess: (widget: Widget, dispatch: Dispatch) => {
      const translatedMessage = firstUpperTranslated(
        'successfully deleted the widget',
        {...widget},
      );
      dispatch(showSuccessMessage(translatedMessage));
    },
    afterFailure: ({message}: ErrorResponse, dispatch: Dispatch) => {
      const translatedMessage = firstUpperTranslated(
        'failed to delete the widget',
        {error: message},
      );
      dispatch(showFailMessage(translatedMessage));
    },
  },
);

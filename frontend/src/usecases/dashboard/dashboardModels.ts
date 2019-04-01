import {WidgetMandatory} from '../../state/domain-models/widget/widgetModels';
import {Callback, CallbackWith} from '../../types/Types';

export interface WidgetDispatchProps {
  deleteWidget: Callback;
  editWidget: Callback;
}

export interface WidgetDispatchers {
  onEdit: CallbackWith<WidgetMandatory>;
  onDelete: CallbackWith<WidgetMandatory>;
}

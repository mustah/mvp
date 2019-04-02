import * as React from 'react';
import {routes} from '../../../app/routes';
import {withWidgetLoader} from '../../../components/hoc/withLoaders';
import {history} from '../../../index';
import {translate} from '../../../services/translationService';
import {CountWidget as CountWidgetModel} from '../../../state/domain-models/widget/widgetModels';
import {initialSelectionId} from '../../../state/user-selection/userSelectionModels';
import {WidgetRequestParameters} from '../../../state/widget/widgetActions';
import {Callback, CallbackWith, CallbackWithId, EncodedUriParameters} from '../../../types/Types';
import {WidgetDispatchers} from '../dashboardModels';
import {IndicatorWidgetProps, NumMetersIndicatorWidget} from './IndicatorWidget';
import {WidgetWithTitle} from './Widget';

export interface OwnProps extends WidgetDispatchers {
  widget: CountWidgetModel;
}

export interface StateToProps {
  isSuccessFullyFetched: boolean;
  isFetching: boolean;
  meterCount: number;
  parameters: EncodedUriParameters;
  title: string;
}

export interface DispatchToProps {
  fetchCountWidget: CallbackWith<WidgetRequestParameters>;
  resetSelection: Callback;
  selectSavedSelection: CallbackWithId;
}

type Props = OwnProps & StateToProps & DispatchToProps;

const CountContentWidgetLoader = withWidgetLoader<IndicatorWidgetProps>(NumMetersIndicatorWidget);

export const CountWidget = ({
  fetchCountWidget,
  isSuccessFullyFetched,
  meterCount,
  onEdit,
  onDelete,
  parameters,
  resetSelection,
  selectSavedSelection,
  title,
  widget,
}: Props) => {
  React.useEffect(() => {
    if (isSuccessFullyFetched) {
      fetchCountWidget({widget, parameters});
    }
  }, [widget, parameters, isSuccessFullyFetched]);

  const deleteWidget = () => onDelete(widget);
  const editWidget = () => onEdit(widget);

  const selectSelection: Callback = () => {
    history.push(routes.meters);
    if (widget.settings.selectionId === initialSelectionId) {
      resetSelection();
    } else {
      selectSavedSelection(widget.settings.selectionId!);
    }
  };

  return (
    <WidgetWithTitle
      title={title}
      editWidget={editWidget}
      deleteWidget={deleteWidget}
      headerClassName="count"
    >
      <CountContentWidgetLoader
        isFetching={!isSuccessFullyFetched}
        onClick={selectSelection}
        title={translate('meter', {count: meterCount})}
        value={meterCount}
      />
    </WidgetWithTitle>
  );
};

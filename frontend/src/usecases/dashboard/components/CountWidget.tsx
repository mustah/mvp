import * as React from 'react';
import {withWidgetLoader} from '../../../components/hoc/withLoaders';
import {translate} from '../../../services/translationService';
import {CountWidget as CountWidgetModel} from '../../../state/domain-models/widget/widgetModels';
import {WidgetRequestParameters} from '../../../state/widget/widgetActions';
import {CallbackWith, EncodedUriParameters, OnClick, uuid} from '../../../types/Types';
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
  selectSelection: CallbackWith<uuid | undefined>;
}

type Props = OwnProps & StateToProps & DispatchToProps;

const CountContentWidgetLoader = withWidgetLoader<IndicatorWidgetProps>(NumMetersIndicatorWidget);

export const CountWidget = ({
  fetchCountWidget,
  isFetching,
  isSuccessFullyFetched,
  meterCount,
  onEdit,
  onDelete,
  parameters,
  selectSelection,
  title,
  widget,
}: Props) => {
  React.useEffect(() => {
    if (isSuccessFullyFetched) {
      fetchCountWidget({widget, parameters});
    }
  }, [widget, parameters, isSuccessFullyFetched, isFetching]);

  const deleteWidget: OnClick = () => onDelete(widget);
  const editWidget: OnClick = () => onEdit(widget);
  const onSelectSelection: OnClick = () => selectSelection(widget.settings.selectionId);

  return (
    <WidgetWithTitle
      title={title}
      editWidget={editWidget}
      deleteWidget={deleteWidget}
      headerClassName="count"
    >
      <CountContentWidgetLoader
        isFetching={isFetching}
        onClick={onSelectSelection}
        title={translate('meter', {count: meterCount})}
        value={meterCount}
      />
    </WidgetWithTitle>
  );
};

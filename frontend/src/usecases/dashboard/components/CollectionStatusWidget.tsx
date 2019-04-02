import * as React from 'react';
import {routes} from '../../../app/routes';
import {withWidgetLoader} from '../../../components/hoc/withLoaders';
import {thresholdClassName} from '../../../helpers/thresholds';
import {history} from '../../../index';
import {translate} from '../../../services/translationService';
import {RequestsHttp} from '../../../state/domain-models/domainModels';
import {CollectionStatusWidget as CollectionStatusWidgetModel} from '../../../state/domain-models/widget/widgetModels';
import {WidgetRequestParameters} from '../../../state/widget/widgetActions';
import {WidgetData} from '../../../state/widget/widgetReducer';
import {Callback, CallbackWith, CallbackWithId, EncodedUriParameters} from '../../../types/Types';
import {WidgetDispatchers} from '../dashboardModels';
import {IndicatorWidget, IndicatorWidgetProps} from './IndicatorWidget';
import {WidgetWithTitle} from './Widget';

export interface OwnProps extends WidgetDispatchers {
  widget: CollectionStatusWidgetModel;
}

export interface StateToProps {
  isUserSelectionsSuccessfullyFetched: boolean;
  isUserSelectionsFetching: boolean;
  model: WidgetData & RequestsHttp;
  parameters: EncodedUriParameters;
  title: string;
}

export interface DispatchToProps {
  fetchCollectionStatsWidget: CallbackWith<WidgetRequestParameters>;
  resetSelection: Callback;
  selectSavedSelection: CallbackWithId;
}

type Props = StateToProps & DispatchToProps & OwnProps;

const LoadingIndicator = withWidgetLoader<IndicatorWidgetProps>(IndicatorWidget);

export const CollectionStatusWidget = (props: Props) => {
  const {
    isUserSelectionsSuccessfullyFetched,
    fetchCollectionStatsWidget,
    widget,
    parameters,
    model,
    isUserSelectionsFetching,
    title,
    onEdit,
    onDelete,
    selectSavedSelection,
    resetSelection,
  } = props;

  React.useEffect(() => {
    if (isUserSelectionsSuccessfullyFetched) {
      fetchCollectionStatsWidget(props);
    }
  }, [widget, parameters, isUserSelectionsSuccessfullyFetched]);

  const count: number = model && model.data;
  const isFetching = model && model.isFetching || isUserSelectionsFetching;

  const onDeleteWidget = () => onDelete(widget);
  const onEditWidget = () => onEdit(widget);
  const onClickWidget = () => {
    if (widget.settings.selectionId) {
      selectSavedSelection(widget.settings.selectionId);
    } else {
      resetSelection();
    }
    history.push(routes.meters);
  };

  return (
    <WidgetWithTitle
      title={title}
      deleteWidget={onDeleteWidget}
      editWidget={onEditWidget}
      headerClassName={thresholdClassName(count)}
    >
      <LoadingIndicator
        isFetching={isFetching}
        onClick={onClickWidget}
        value={count}
        title={translate('collection')}
      />
    </WidgetWithTitle>
  );
};

import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {routes} from '../../../app/routes';
import {withWidgetLoader} from '../../../components/hoc/withLoaders';
import {IndicatorWidgetProps, NumMetersIndicatorWidget} from '../components/IndicatorWidget';
import {history} from '../../../index';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {CountableWidgetModel, CountWidget, WidgetMandatory} from '../../../state/domain-models/widget/widgetModels';
import {resetSelection, selectSavedSelection} from '../../../state/user-selection/userSelectionActions';
import {initialSelectionId} from '../../../state/user-selection/userSelectionModels';
import {allCurrentMeterParameters, getMeterParameters} from '../../../state/user-selection/userSelectionSelectors';
import {fetchCountWidget, WidgetRequestParameters} from '../../../state/widget/widgetActions';
import {WidgetState} from '../../../state/widget/widgetReducer';
import {Callback, CallbackWith, CallbackWithId, EncodedUriParameters, OnClick, uuid} from '../../../types/Types';
import {WidgetWithTitle} from '../components/Widget';

interface OwnProps {
  widget: CountWidget;
  openConfiguration: OnClick;
  onDelete: CallbackWith<WidgetMandatory>;
}

interface StateToProps {
  title: string;
  isSuccessFullyFetched: boolean;
  parameters: EncodedUriParameters;
  meterCount: number;
}

interface DispatchToProps {
  fetchCountWidget: CallbackWith<WidgetRequestParameters>;
  resetSelection: Callback;
  selectSavedSelection: CallbackWithId;
}

const getMeterCount = (data: WidgetState, id: uuid): number => {
  if (data[id] !== undefined && data[id].data !== undefined) {
    return data[id].data || 0;
  }
  return 0;
};

const mapStateToProps = (
  {domainModels: {userSelections}, widget}: RootState,
  {widget: {settings: {selectionId}, id}}: OwnProps
): StateToProps => {
  const userSelection = selectionId && userSelections.entities[selectionId];

  const parameters = userSelection
    ? getMeterParameters({userSelection})
    : allCurrentMeterParameters;

  const title = userSelection
    ? userSelection.name
    : translate('all meters');

  return {
    isSuccessFullyFetched: userSelections.isSuccessfullyFetched,
    title,
    parameters,
    meterCount: getMeterCount(widget, id)
  };
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  fetchCountWidget,
  resetSelection,
  selectSavedSelection,
}, dispatch);

const CountContentWidgetLoader = withWidgetLoader<IndicatorWidgetProps>(NumMetersIndicatorWidget);

type Props = OwnProps & StateToProps & DispatchToProps;

const CountWidget = ({
  isSuccessFullyFetched,
  title,
  widget,
  openConfiguration,
  parameters,
  onDelete,
  fetchCountWidget,
  meterCount,
  resetSelection,
  selectSavedSelection,
}: Props) => {
  React.useEffect(() => {
    if (isSuccessFullyFetched) {
      fetchCountWidget({widget, parameters});
    }
  }, [widget, parameters, isSuccessFullyFetched]);

  const deleteWidget = () => onDelete(widget);

  const widgetModel: CountableWidgetModel = {count: meterCount};

  const selectSelection: Callback = () => {
    history.push(routes.meters);
    if (widget.settings.selectionId === initialSelectionId) {
      resetSelection();
    } else {
      selectSavedSelection(widget.settings.selectionId!);
    }
  };

  return (
    <WidgetWithTitle title={title} configure={openConfiguration} deleteWidget={deleteWidget} headerClassName="count">
      <div onClick={selectSelection} className="clickable">
        <CountContentWidgetLoader
          widget={widgetModel}
          isFetching={!isSuccessFullyFetched}
          title={translate('meter', {count: meterCount})}
        />
      </div>
    </WidgetWithTitle>
  );
};

export const CountWidgetContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(mapStateToProps, mapDispatchToProps)(CountWidget);

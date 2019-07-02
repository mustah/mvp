import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {resetSelection, selectSelection} from '../../../state/user-selection/userSelectionActions';
import {allCurrentMeterParameters, getMeterParameters} from '../../../state/user-selection/userSelectionSelectors';
import {fetchCountWidget} from '../../../state/widget/widgetActions';
import {WidgetModel} from '../../../state/widget/widgetReducer';
import {CountWidget, DispatchToProps, OwnProps, StateToProps} from '../components/CountWidget';
import {getMeterCount, isFetching} from '../dashboardSelectors';

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

  const widgetModel: WidgetModel = widget[id];
  return {
    isSuccessFullyFetched: userSelections.isSuccessfullyFetched,
    isFetching: userSelections.isFetching || isFetching(widgetModel),
    title,
    parameters,
    meterCount: getMeterCount(widgetModel)
  };
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  fetchCountWidget,
  resetSelection,
  selectSelection,
}, dispatch);

export const CountWidgetContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(mapStateToProps, mapDispatchToProps)(CountWidget);

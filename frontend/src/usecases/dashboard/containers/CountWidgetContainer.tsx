import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {resetSelection, selectSavedSelection} from '../../../state/user-selection/userSelectionActions';
import {allCurrentMeterParameters, getMeterParameters} from '../../../state/user-selection/userSelectionSelectors';
import {fetchCountWidget} from '../../../state/widget/widgetActions';
import {CountWidget, DispatchToProps, OwnProps, StateToProps} from '../components/CountWidget';
import {getMeterCount} from '../dashboardSelectors';

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
    isFetching: userSelections.isFetching,
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

export const CountWidgetContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(mapStateToProps, mapDispatchToProps)(CountWidget);

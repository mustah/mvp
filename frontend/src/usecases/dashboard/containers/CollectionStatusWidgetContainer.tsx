import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {makeCollectionPeriodParametersOf} from '../../../helpers/urlFactory';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {clearCollectionStatsError as clearError} from '../../../state/domain-models-paginated/collection-stat/collectionStatActions';
import {deleteWidget} from '../../../state/domain-models/widget/widgetActions';
import {resetSelection, selectSelection} from '../../../state/user-selection/userSelectionActions';
import {getApiParameters} from '../../../state/user-selection/userSelectionSelectors';
import {fetchCollectionStatsWidget} from '../../../state/widget/widgetActions';
import {CollectionStatusWidget, DispatchToProps, OwnProps, StateToProps} from '../components/CollectionStatusWidget';

const mapStateToProps = (
  {domainModels: {userSelections}, widget}: RootState,
  {widget: {settings: {selectionInterval, selectionId}, id}}: OwnProps
): StateToProps => {
  const userSelection = selectionId && userSelections.entities[selectionId];

  // TODO: fix
  const parameters =
    userSelection
      ? makeCollectionPeriodParametersOf(selectionInterval) + '&' + getApiParameters({
      userSelection: {
        ...userSelection,
        selectionParameters: {
          ...userSelection.selectionParameters,
        },
      },
    })
      : makeCollectionPeriodParametersOf(selectionInterval);

  const title = userSelection
    ? userSelection.name
    : translate('all meters');

  return {
    model: widget[id],
    parameters,
    isUserSelectionsSuccessfullyFetched: userSelections.isSuccessfullyFetched,
    isUserSelectionsFetching: userSelections.isFetching,
    title,
  };
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  clearError,
  fetchCollectionStatsWidget,
  selectSelection,
  resetSelection,
  deleteWidget,
}, dispatch);

export const CollectionStatusWidgetContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(mapStateToProps, mapDispatchToProps)(CollectionStatusWidget);

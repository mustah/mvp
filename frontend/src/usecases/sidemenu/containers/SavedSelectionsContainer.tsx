import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {isMeterPage} from '../../../selectors/routerSelectors';
import {NormalizedState} from '../../../state/domain-models/domainModels';
import {
  deleteUserSelection,
  fetchUserSelections,
  resetSelection,
  selectSavedSelection,
  selectSelection,
} from '../../../state/user-selection/userSelectionActions';
import {UserSelection} from '../../../state/user-selection/userSelectionModels';
import {getUserSelection} from '../../../state/user-selection/userSelectionSelectors';
import {Callback, CallbackWithId, OnClick, OnClickWith, OnClickWithId} from '../../../types/Types';
import {addToReport} from '../../../state/report/reportActions';
import {LegendItem} from '../../../state/report/reportModels';
import {SavedSelections} from '../components/SavedSelections';

export interface StateToProps {
  selection: UserSelection;
  savedSelections: NormalizedState<UserSelection>;
  isMeterPage: boolean;
}

export interface DispatchToProps {
  deleteUserSelection: CallbackWithId;
  fetchUserSelections: Callback;
  resetSelection: OnClick;
  selectSelection: OnClickWithId;
  selectSavedSelection: OnClickWithId;
  addToReport: OnClickWith<LegendItem>;
}

const mapStateToProps =
  ({userSelection, domainModels: {userSelections}, routing}: RootState): StateToProps => ({
    selection: getUserSelection(userSelection),
    savedSelections: userSelections,
    isMeterPage: isMeterPage(routing),
  });

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  addToReport,
  deleteUserSelection,
  fetchUserSelections,
  resetSelection,
  selectSelection,
  selectSavedSelection,
}, dispatch);

export const SavedSelectionsContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(SavedSelections);

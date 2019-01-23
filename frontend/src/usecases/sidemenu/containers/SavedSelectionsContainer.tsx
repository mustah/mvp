import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {NormalizedState} from '../../../state/domain-models/domainModels';
import {
  deleteUserSelection,
  fetchUserSelections,
  selectSavedSelection,
} from '../../../state/user-selection/userSelectionActions';
import {UserSelection} from '../../../state/user-selection/userSelectionModels';
import {getUserSelection} from '../../../state/user-selection/userSelectionSelectors';
import {Callback, CallbackWithId, OnClick} from '../../../types/Types';
import {SavedSelections} from '../components/SavedSelections';

export interface StateToProps {
  selection: UserSelection;
  savedSelections: NormalizedState<UserSelection>;
  isFetching: boolean;
}

export interface DispatchToProps {
  deleteUserSelection: CallbackWithId;
  fetchUserSelections: Callback;
  selectSavedSelection: OnClick;
}

const mapStateToProps =
  ({userSelection, domainModels: {userSelections}}: RootState): StateToProps => ({
    selection: getUserSelection(userSelection),
    savedSelections: userSelections,
    isFetching: userSelections.isFetching,
  });

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  selectSavedSelection,
  fetchUserSelections,
  deleteUserSelection,
}, dispatch);

export const SavedSelectionsContainer =
  connect<StateToProps, DispatchToProps>(
    mapStateToProps,
    mapDispatchToProps
  )(SavedSelections);

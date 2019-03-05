import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {
  closeSelectionPage,
  resetSelection,
  saveSelection,
  selectSavedSelection,
  updateSelection,
} from '../../../state/user-selection/userSelectionActions';
import {UserSelection} from '../../../state/user-selection/userSelectionModels';
import {getUserSelection} from '../../../state/user-selection/userSelectionSelectors';
import {CallbackWith, OnClick, OnClickWithId} from '../../../types/Types';
import {SelectionMenu} from '../components/selection-menu/SelectionMenu';

interface StateToProps {
  selection: UserSelection;
}

interface DispatchToProps {
  closeSelectionPage: OnClick;
  saveSelection: CallbackWith<UserSelection>;
  updateSelection: CallbackWith<UserSelection>;
  resetSelection: OnClick;
  selectSavedSelection: OnClickWithId;
}

export type SelectionMenuProps = StateToProps & DispatchToProps;

const mapStateToProps = ({userSelection}: RootState): StateToProps => ({
  selection: getUserSelection(userSelection),
});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  closeSelectionPage,
  saveSelection,
  updateSelection,
  resetSelection,
  selectSavedSelection,
}, dispatch);

export const SelectionMenuContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(SelectionMenu);

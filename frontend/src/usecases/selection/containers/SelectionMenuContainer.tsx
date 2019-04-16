import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {
  resetSelection,
  resetToSavedSelection,
  saveSelection,
  updateSelection
} from '../../../state/user-selection/userSelectionActions';
import {getUserSelection} from '../../../state/user-selection/userSelectionSelectors';
import {DispatchToProps, SelectionMenu, StateToProps} from '../components/selection-menu/SelectionMenu';

const mapStateToProps = ({userSelection}: RootState): StateToProps => ({
  selection: getUserSelection(userSelection),
});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  resetSelection,
  resetToSavedSelection,
  saveSelection,
  updateSelection,
}, dispatch);

export const SelectionMenuContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(SelectionMenu);

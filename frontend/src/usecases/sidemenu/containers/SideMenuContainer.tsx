import {connect} from 'react-redux';
import {RootState} from '../../../reducers/rootReducer';
import {isSideMenuOpen} from '../../../state/ui/uiSelectors';
import {WithChildren} from '../../../types/Types';
import {SideMenu} from '../components/SideMenu';

export interface StateToProps {
  isSideMenuOpen: boolean;
}

const mapStateToProps = ({ui}: RootState): StateToProps => ({
  isSideMenuOpen: isSideMenuOpen(ui),
});

export const SideMenuContainer = connect<StateToProps, {}, WithChildren>(mapStateToProps)(SideMenu);

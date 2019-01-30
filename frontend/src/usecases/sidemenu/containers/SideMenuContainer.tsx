import * as React from 'react';
import {connect} from 'react-redux';
import {RootState} from '../../../reducers/rootReducer';
import {isSideMenuOpen} from '../../../state/ui/uiSelectors';
import {WithChildren} from '../../../types/Types';
import {SideMenu} from '../components/SideMenu';
import '../components/SideMenu.scss';

export interface StateToProps {
  isSideMenuOpen: boolean;
}

export interface OwnProps extends WithChildren {
  containerStyle?: React.CSSProperties;
}

const mapStateToProps = ({ui}: RootState): StateToProps => ({
  isSideMenuOpen: isSideMenuOpen(ui),
});

export const SideMenuContainer = connect<StateToProps, {}, OwnProps>(mapStateToProps)(SideMenu);

import * as React from 'react';
import {connect} from 'react-redux';
import {RootState} from '../../reducers/rootReducer';
import {isSideMenuOpen} from '../../state/ui/uiSelectors';

export interface StateToProps {
  isSideMenuOpen: boolean;
}

const mapStateToProps = ({ui}: RootState): StateToProps => ({
  isSideMenuOpen: isSideMenuOpen(ui),
});

export const withSideMenu =
  <OwnProps extends {}>(Component: React.ComponentType<OwnProps & StateToProps>) =>
    connect<StateToProps, {}, OwnProps>(mapStateToProps)(Component);

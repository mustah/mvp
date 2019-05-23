import * as React from 'react';
import {connect} from 'react-redux';
import {compose} from 'recompose';
import {RootState} from '../../reducers/rootReducer';
import {isSideMenuOpen} from '../../state/ui/uiSelectors';

export interface StateToProps {
  isSideMenuOpen: boolean;
}

const mapStateToProps = ({ui}: RootState): StateToProps => ({
  isSideMenuOpen: isSideMenuOpen(ui),
});

export const withSideMenu =
  <P extends {}>(Component: React.ComponentType<P & StateToProps>) =>
    compose<StateToProps, P>(connect(mapStateToProps))(Component);

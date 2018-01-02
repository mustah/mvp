import 'AdminApp.scss';
import NavigationMenu from 'material-ui/svg-icons/navigation/menu';
import * as React from 'react';
import {connect} from 'react-redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history3/redirect';
import {Column, ColumnBottom} from '../../components/layouts/column/Column';
import {Row} from '../../components/layouts/row/Row';
import {RootState} from '../../reducers/rootReducer';
import {isSideMenuOpen} from '../../state/ui/uiSelectors';
import {colors} from '../themes';
import {AdminPages} from './AdminPages';

interface StateToProps {
  isAuthenticated: boolean;
  isSideMenuOpen: boolean;
}

type Props = StateToProps & InjectedAuthRouterProps;

const AdminApp = () => (
  <Row className="AdminApp">
    <Column className="Admin-side-container">
      <ColumnBottom className="app-switch-position">
        <NavigationMenu color={colors.white} className="MenuButton clickable"/>
      </ColumnBottom>
    </Column>
    <AdminPages/>
  </Row>
);

const mapStateToProps = ({auth: {isAuthenticated}, ui}: RootState) => ({
  isAuthenticated,
  isSideMenuOpen: isSideMenuOpen(ui),
});

export const AdminAppContainer = connect<StateToProps, {}, Props>(mapStateToProps)(AdminApp);

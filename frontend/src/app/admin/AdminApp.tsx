import 'AdminApp.scss';
import * as React from 'react';
import {connect} from 'react-redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history3/redirect';
import {AppSwitchDropdown} from '../../components/actions-dropdown/AppSwitchDropdown';
import {Column, ColumnBottom} from '../../components/layouts/column/Column';
import {Layout} from '../../components/layouts/layout/Layout';
import {Row} from '../../components/layouts/row/Row';
import {RootState} from '../../reducers/rootReducer';
import {isSideMenuOpen} from '../../state/ui/uiSelectors';
import {AdminPages} from './AdminPages';

interface StateToProps {
  isSideMenuOpen: boolean;
}

type Props = StateToProps & InjectedAuthRouterProps;

const AdminApp = () => {

  return (
    <Row className="AdminApp">
      <Column className="Admin-side-container">
        <ColumnBottom className="flex-1">
          <AppSwitchDropdown/>
        </ColumnBottom>
      </Column>
      <Layout style={{width: 64}}/>
      <AdminPages/>
    </Row>
  );
};

const mapStateToProps = ({ui}: RootState) => ({
  isSideMenuOpen: isSideMenuOpen(ui),
});

export const AdminAppContainer = connect<StateToProps, {}, Props>(mapStateToProps)(AdminApp);
